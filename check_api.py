import requests
import time

print("Waiting for Spring Boot to start...")
time.sleep(5) # wait for app to be ready

endpoints = [
    "http://localhost:8080/api/products?page=0&size=5",
    "http://localhost:8080/api/analytics/top-products?limit=5",
    "http://localhost:8080/api/inventory/alerts"
]

for url in endpoints:
    print(f"Testing {url} ...")
    try:
        response = requests.get(url, timeout=5)
        print(f"Status: {response.status_code}")
        try:
            print(response.json()[:2] if isinstance(response.json(), list) else str(response.json())[:500])
        except Exception:
            print(response.text[:200])
    except Exception as e:
        print(f"Error: {e}")
    print("-" * 50)
