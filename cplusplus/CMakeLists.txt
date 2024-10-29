cmake_minimum_required(VERSION 3.10)
project(ContentfulRequestVerification)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Set build type to Release by default if none specified
if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Release)
endif()

# Find Drogon package
find_package(Drogon REQUIRED)

# Set the source files
set(SOURCES
    main.cpp
)

# Define the executable
add_executable(contentful_request_verification ${SOURCES})

# Link Drogon library
target_link_libraries(contentful_request_verification PRIVATE Drogon::Drogon)
