from urllib.parse import urljoin

from googlesearch import search
import requests
from bs4 import BeautifulSoup

def google_search(query, max_results=10):
    search_results = search(query, stop=max_results)
    product_links = set()  # Use a set to avoid duplicates
    collection_links = set()

    for result in search_results:
        if "/products/" in result:
            product_links.add(result)  # Product links contain /products/
        elif "/collections/" in result:
            collection_links.add(result)  # Collection links contain /collections/

    print("\nFiltered Product Links:")
    for i, link in enumerate(product_links, start=1):
        print(f"Product {i}: {link}")

    print("\nFiltered Collection Links:")
    for i, link in enumerate(collection_links, start=1):
        print(f"Collection {i}: {link}")

    return product_links, collection_links

def extract_product_links_from_collection(collection_url):
    try:
        response = requests.get(collection_url)
        response.raise_for_status()
        soup = BeautifulSoup(response.text, 'html.parser')

        # Find and process product links
        base_url = collection_url.split("/collections")[0]
        product_links = set()
        for a in soup.find_all('a', href=True):
            href = a['href']
            if '/products/' in href:
                # Extract only the part starting with '/products/'
                product_path = href[href.index('/products/'):]
                # Build the absolute URL using urljoin
                absolute_url = urljoin(base_url, product_path)
                product_links.add(absolute_url)

        return product_links
    except Exception as e:
        print(f"Error scraping collection page {collection_url}: {e}")
        return set()

def extract_links_for_query(query, max_results=10, extract_collections=False):
    all_product_links = set()
    # query like site:myshopify.com "{keyword}"
    # query = input("Enter a search query: ")
    product_links, collection_links = google_search(query, max_results)

    all_product_links.update(product_links)

    if extract_collections:
        for link in collection_links:
            extracted_products = extract_product_links_from_collection(link)
            all_product_links.update(extracted_products)

    # Print all unique product links
    print("\nAll Unique Product Links:")
    for i, link in enumerate(all_product_links, start=1):
        print(f"Product {i}: {link}")

    return all_product_links



