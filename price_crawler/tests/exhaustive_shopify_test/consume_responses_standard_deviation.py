import csv
from confluent_kafka import Consumer
import json
import signal
import sys
import math

# Global stop flag
stop_signal = False

def signal_handler(sig, frame):
    global stop_signal
    print("Stopping...")
    stop_signal = True

def consume_responses(csv_path):
    # Read product information from the CSV
    products = {}
    with open(csv_path, mode="r") as csvfile:
        reader = csv.reader(csvfile, delimiter=';')
        next(reader)  # Skip header
        for row in reader:
            keyword, allowed_deviation, job_id_prefix = row
            products[job_id_prefix] = {
                "keyword": keyword,
                "success_count": 0,
                "failure_count": 0,
                "total_price": 0.0,
                "price_list": []  # To store prices for standard deviation calculation
            }

    # Overall counters
    total_successes = 0
    total_failures = 0

    # Configure Kafka consumer
    consumer = Consumer({
        'bootstrap.servers': 'localhost:9092',  # Adjust to your Kafka broker address
        'group.id': 'test-group',  # Consumer group name
        'auto.offset.reset': 'earliest'
    })

    # Subscribe to the response topic
    consumer.subscribe(['crawl.response'])

    try:
        while not stop_signal:
            msg = consumer.poll(timeout=1.0)

            if msg is None:
                continue  # No message, continue waiting
            if msg.error():
                print(f"Error consuming message: {msg.error()}")
                continue

            # Parse the response message
            response_data = json.loads(msg.value().decode('utf-8'))
            job_id = str(response_data['jobId']['productId'])  # Convert to string for prefix matching
            status = response_data['status']
            price = response_data.get('crawledPrice', {}).get('price', None)

            # Match the Job ID prefix to the product
            prefix = job_id[:4]
            if prefix not in products:
                print(f"Unknown Job ID prefix: {prefix}")
                continue

            product_info = products[prefix]

            if status == 'FAILURE':
                product_info["failure_count"] += 1
                total_failures += 1
            elif status == 'SUCCESS' and price is not None:
                product_info["success_count"] += 1
                product_info["total_price"] += price
                product_info["price_list"].append(price)
                total_successes += 1

            print(f"Processed response for {product_info['keyword']} (Job ID: {job_id})")

    except KeyboardInterrupt:
        print("Consuming stopped.")
    finally:
        consumer.close()

        # Print final statistics
        print("\nFinal Statistics:")
        for prefix, product_info in products.items():
            if product_info["success_count"] > 0:
                avg_price = product_info["total_price"] / product_info["success_count"]

                # Calculate standard deviation
                variance = sum((p - avg_price) ** 2 for p in product_info["price_list"]) / len(product_info["price_list"])
                std_deviation = math.sqrt(variance)

                # Count prices within one standard deviation
                within_deviation_count = sum(
                    1 for price in product_info["price_list"]
                    if abs(price - avg_price) <= std_deviation
                )

                print(f"Product: {product_info['keyword']}")
                print(f"  Total Successes: {product_info['success_count']}")
                print(f"  Total Failures: {product_info['failure_count']}")
                print(f"  Average Price: {avg_price:.2f}")
                print(f"  Standard Deviation: {std_deviation:.2f}")
                print(f"  Prices within 1 Std Dev: {within_deviation_count}/{len(product_info['price_list'])}\n")
            else:
                print(f"Product: {product_info['keyword']}")
                print(f"  Total Successes: {product_info['success_count']}")
                print(f"  Total Failures: {product_info['failure_count']}")
                print(f"  Average Price: N/A")
                print(f"  Standard Deviation: N/A")
                print(f"  Prices within 1 Std Dev: N/A\n")

        # Calculate overall success rate
        total_responses = total_successes + total_failures
        if total_responses > 0:
            overall_success_rate = (total_successes / total_responses) * 100
        else:
            overall_success_rate = 0.0

        print(f"Overall Statistics:")
        print(f"  Total Successes: {total_successes}")
        print(f"  Total Failures: {total_failures}")
        print(f"  Overall Success Rate: {overall_success_rate:.2f}%")

if __name__ == "__main__":
    # Register the signal handler for Ctrl+C
    signal.signal(signal.SIGINT, signal_handler)
    consume_responses("general_product_keywords.csv")
