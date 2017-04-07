#!/bin/sh
URL='http://localhost:8080/ocd'
echo "curl test script for las2peer service"

echo "test authentication with test user"
curl -v -X GET $URL/validate --user User:user
echo 
echo "PRESS RETURN TO CONTINUE..."
read

echo "more curl commandlines..."


