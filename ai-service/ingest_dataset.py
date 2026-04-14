import pandas as pd
from sqlalchemy import create_engine, text
import datetime
import random
import sys

if __name__ == "__main__":
    print("Connecting to SQLite ai_business.db database...")
    import os
    # Connect to db file in data folder, correctly pathing up from ai-service
    engine = create_engine('sqlite:///../data/ai_business.db')

    print("Reading Excel file... (this heavily depends on CPU/RAM, typically 1-3 mins)")
    try:
        # Load just 50,000 rows to speed things up for the demonstration instead of 500,000
        df = pd.read_excel('../data/Online Retail Data Set.xlsx', engine='openpyxl', nrows=50000)
    except Exception as e:
        print(f"Failed to read dataset: {e}")
        sys.exit(1)

    print(f"Loaded {len(df)} rows. Cleaning data...")
    df = df[df['Quantity'] > 0]
    df = df[df['UnitPrice'] > 0]
    df['Description'] = df['Description'].fillna('Unknown Product')

    print("Transforming Products...")
    products_df = df[['StockCode', 'Description', 'UnitPrice']].drop_duplicates(subset=['StockCode']).copy()
    products_df['product_id'] = range(1, len(products_df) + 1)
    
    products_out = pd.DataFrame({
        'product_id': products_df['product_id'],
        'name': products_df['Description'].str.slice(0, 200),
        'category': 'General Retail',
        'unit_price': products_df['UnitPrice'],
        'restock_threshold': [random.randint(10, 50) for _ in range(len(products_df))],
        'created_at': datetime.datetime.now()
    })

    print("Transforming Inventory...")
    zones = ['ZONE_A', 'ZONE_B', 'ZONE_C', 'ZONE_D']
    inventory_out = pd.DataFrame({
        'inventory_id': range(1, len(products_df) + 1),
        'product_id': products_df['product_id'],
        'quantity_on_hand': [random.randint(2, 60) for _ in range(len(products_df))],
        'warehouse_zone': [random.choice(zones) for _ in range(len(products_df))],
        'last_updated': datetime.datetime.now()
    })

    print("Transforming Sales...")
    stockcode_to_id = dict(zip(products_df['StockCode'], products_df['product_id']))
    df['product_id'] = df['StockCode'].map(stockcode_to_id)
    
    sales_out = pd.DataFrame({
        'sale_id': range(1, len(df) + 1),
        'product_id': df['product_id'],
        'quantity_sold': df['Quantity'],
        'sale_date': pd.to_datetime(df['InvoiceDate']).dt.date,
        'revenue': df['Quantity'] * df['UnitPrice'],
        'channel': 'ONLINE'
    })

    print("Generating Synthetic Reviews...")
    # Generate ~2000 reviews for random products
    review_samples = products_out.sample(n=min(2000, len(products_out)), replace=True).reset_index()
    reviews_out = pd.DataFrame({
        'review_id': range(1, len(review_samples) + 1),
        'product_id': review_samples['product_id'],
        'review_text': ["Great product, fast shipping!" if random.random() > 0.3 else "Slightly damaged packaging." for _ in range(len(review_samples))],
        'rating': [random.choice([4, 5]) if random.random() > 0.3 else random.choice([1, 2, 3]) for _ in range(len(review_samples))],
        'sentiment_score': [random.uniform(0.5, 1.0) if random.random() > 0.3 else random.uniform(0.0, 0.4) for _ in range(len(review_samples))],
        'review_date': pd.to_datetime(datetime.datetime.now().date()) - pd.to_timedelta([random.randint(1, 30) for _ in range(len(review_samples))], unit='D')
    })
    
    print("Writing records to SQLite DB (this will wipe existing records to avoid duplicates in this demo)...")
    try:
        with engine.begin() as conn:
            conn.execute(text("DELETE FROM delivery_routes;"))
            conn.execute(text("DELETE FROM reviews;"))
            conn.execute(text("DELETE FROM sales;"))
            conn.execute(text("DELETE FROM inventory;"))
            conn.execute(text("DELETE FROM products;"))
            conn.execute(text("DELETE FROM orders;"))
    except Exception as e:
        print(f"Warning on delete: {e}")

    products_out.to_sql('products', engine, if_exists='append', index=False)
    print(f"Inserted {len(products_out)} products.")
    
    inventory_out.to_sql('inventory', engine, if_exists='append', index=False)
    print(f"Inserted {len(inventory_out)} inventory records.")
    
    sales_out.to_sql('sales', engine, if_exists='append', index=False)
    print(f"Inserted {len(sales_out)} sales.")
    
    reviews_out.to_sql('reviews', engine, if_exists='append', index=False)
    print(f"Inserted {len(reviews_out)} reviews.")

    # No sequence syncing needed for SQLite in this context

    print("Dataset integration successfully completed!")
