import csv
from confluent_kafka import Consumer
import json
import signal
import sys

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
                "allowed_deviation": float(allowed_deviation),
                "success_count": 0,
                "failure_count": 0,
                "total_price": 0.0,
                "price_list": []  # To store prices for deviation calculation
            }

    # Overall counters
    total_successes = 0
    total_failures = 0
    overall_within_deviation = 0  # Count of prices within allowed deviation across all products

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

                # Check if the price is within the allowed deviation
                avg_price = product_info["total_price"] / product_info["success_count"]
                if abs(price - avg_price) <= (avg_price * (product_info["allowed_deviation"] / 100)):
                    overall_within_deviation += 1

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
            else:
                avg_price = 0.0

            # Calculate deviation percentage
            deviation_count = sum(
                1 for price in product_info["price_list"]
                if abs(price - avg_price) <= (avg_price * (product_info["allowed_deviation"] / 100))
            )

            print(f"Product: {product_info['keyword']}")
            print(f"  Total Successes: {product_info['success_count']}")
            print(f"  Total Failures: {product_info['failure_count']}")
            print(f"  Average Price: {avg_price:.2f}")
            print(f"  Prices within allowed deviation: {deviation_count}/{len(product_info['price_list'])}\n")

        # Calculate overall success rate and deviation success rate
        total_responses = total_successes + total_failures
        if total_responses > 0:
            overall_success_rate = (total_successes / total_responses) * 100
        else:
            overall_success_rate = 0.0

        if total_successes > 0:
            overall_deviation_rate = (overall_within_deviation / total_successes) * 100
        else:
            overall_deviation_rate = 0.0

        print(f"Overall Statistics:")
        print(f"  Total Successes: {total_successes}")
        print(f"  Total Failures: {total_failures}")
        print(f"  Overall Success Rate: {overall_success_rate:.2f}%")
        print(f"  Overall Prices Within Allowed Deviation: {overall_deviation_rate:.2f}%")

if __name__ == "__main__":
    # Register the signal handler for Ctrl+C
    signal.signal(signal.SIGINT, signal_handler)
    consume_responses("general_product_keywords.csv")
