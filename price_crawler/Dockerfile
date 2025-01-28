# Base image
FROM ubuntu:22.04
FROM python:3.12.7-bookworm

# Update and install required tools
RUN apt-get update && \
    apt-get install -y unzip wget curl && \
    apt-get clean

# Download and install Chrome
RUN wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    apt install -y ./google-chrome-stable_current_amd64.deb && \
    rm -f google-chrome-stable_current_amd64.deb

# Download and set up ChromeDriver

# Verify Chrome installation
RUN google-chrome --version

# Set up the working directory
WORKDIR /crawler-service/crawler

# Copy and install Python dependencies
COPY requirements.txt /crawler-service/crawler/
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of the application
COPY . /crawler-service/crawler

# Set the PYTHONPATH environment variable
ENV PYTHONPATH=/crawler-service

# Default command to run the crawler
CMD ["python", "main.py"]
