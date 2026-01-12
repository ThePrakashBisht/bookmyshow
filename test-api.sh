#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"
TOKEN=""

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  BookMyShow API End-to-End Tests      ${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to make requests and check response
check_response() {
    local description=$1
    local expected_status=$2
    local actual_status=$3
    local response=$4

    if [ "$actual_status" == "$expected_status" ]; then
        echo -e "${GREEN}✓ $description - Status: $actual_status${NC}"
    else
        echo -e "${RED}✗ $description - Expected: $expected_status, Got: $actual_status${NC}"
        echo -e "${RED}  Response: $response${NC}"
    fi
}

# ============ Health Check ============
echo -e "\n${YELLOW}--- Health Checks ---${NC}"

response=$(curl -s -w "\n%{http_code}" "$BASE_URL/health")
status=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
check_response "Gateway Health Check" "200" "$status" "$body"

response=$(curl -s -w "\n%{http_code}" "$BASE_URL/actuator/health")
status=$(echo "$response" | tail -n1)
check_response "Actuator Health" "200" "$status" ""

# ============ Public Endpoints ============
echo -e "\n${YELLOW}--- Public Endpoints (No Auth Required) ---${NC}"

# Get Cities
response=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/cities")
status=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
check_response "Get All Cities" "200" "$status" "$body"

# Get Events
response=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/events")
status=$(echo "$response" | tail -n1)
check_response "Get All Events" "200" "$status" ""

# Get Events by Type
response=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/events/type/MOVIE")
status=$(echo "$response" | tail -n1)
check_response "Get Movies" "200" "$status" ""

# ============ Authentication ============
echo -e "\n${YELLOW}--- Authentication ---${NC}"

# Register User
RANDOM_EMAIL="testuser_$(date +%s)@test.com"
register_response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "{
        \"email\": \"$RANDOM_EMAIL\",
        \"password\": \"Password@123\",
        \"firstName\": \"Test\",
        \"lastName\": \"User\",
        \"phoneNumber\": \"9876543210\"
    }")
status=$(echo "$register_response" | tail -n1)
body=$(echo "$register_response" | sed '$d')
check_response "User Registration" "201" "$status" "$body"

# Extract token from registration response
TOKEN=$(echo "$body" | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"//;s/"//')

if [ -z "$TOKEN" ]; then
    echo -e "${YELLOW}Registration might have failed (user exists?), trying login...${NC}"

    # Try login
    login_response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{
            \"email\": \"john.doe@example.com\",
            \"password\": \"Password@123\"
        }")
    status=$(echo "$login_response" | tail -n1)
    body=$(echo "$login_response" | sed '$d')
    check_response "User Login" "200" "$status" "$body"

    TOKEN=$(echo "$body" | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"//;s/"//')
fi

if [ -n "$TOKEN" ]; then
    echo -e "${GREEN}✓ Token acquired successfully${NC}"
else
    echo -e "${RED}✗ Failed to acquire token${NC}"
fi

# ============ Protected Endpoints ============
echo -e "\n${YELLOW}--- Protected Endpoints (Auth Required) ---${NC}"

# Access without token
response=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/users/profile")
status=$(echo "$response" | tail -n1)
check_response "Get Profile (No Token) - Should fail" "401" "$status" ""

# Access with token
if [ -n "$TOKEN" ]; then
    response=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/users/profile" \
        -H "Authorization: Bearer $TOKEN")
    status=$(echo "$response" | tail -n1)
    check_response "Get Profile (With Token)" "200" "$status" ""
fi

# ============ Booking Flow ============
echo -e "\n${YELLOW}--- Booking Flow ---${NC}"

if [ -n "$TOKEN" ]; then
    # Get available shows
    response=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/shows")
    status=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    check_response "Get Shows" "200" "$status" ""

    # Try to initiate booking
    booking_response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/bookings/initiate" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "{
            \"showId\": 1,
            \"showSeatIds\": [1, 2],
            \"userId\": 1
        }")
    status=$(echo "$booking_response" | tail -n1)
    body=$(echo "$booking_response" | sed '$d')

    if [ "$status" == "201" ] || [ "$status" == "200" ]; then
        check_response "Initiate Booking" "200/201" "$status" ""

        # Extract booking ID
        BOOKING_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')

        if [ -n "$BOOKING_ID" ]; then
            echo -e "${GREEN}  Booking ID: $BOOKING_ID${NC}"

            # Get booking details
            response=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/bookings/$BOOKING_ID" \
                -H "Authorization: Bearer $TOKEN")
            status=$(echo "$response" | tail -n1)
            check_response "Get Booking Details" "200" "$status" ""
        fi
    else
        echo -e "${YELLOW}⚠ Booking initiation returned: $status (seats might be taken)${NC}"
    fi
fi

# ============ Rate Limiting Test ============
echo -e "\n${YELLOW}--- Rate Limiting Test ---${NC}"

echo "Making 15 rapid requests..."
success_count=0
rate_limited_count=0

for i in {1..15}; do
    response=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/cities")
    status=$(echo "$response" | tail -n1)

    if [ "$status" == "200" ]; then
        ((success_count++))
    elif [ "$status" == "429" ]; then
        ((rate_limited_count++))
    fi
done

echo -e "${GREEN}✓ Successful: $success_count${NC}"
echo -e "${YELLOW}⚠ Rate Limited: $rate_limited_count${NC}"

# ============ Circuit Breaker Test ============
echo -e "\n${YELLOW}--- Circuit Breaker Status ---${NC}"

response=$(curl -s "$BASE_URL/actuator/circuitbreakers")
echo "$response" | grep -o '"state":"[^"]*"' | head -3

# ============ Summary ============
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}  Test Complete!                        ${NC}"
echo -e "${BLUE}========================================${NC}"