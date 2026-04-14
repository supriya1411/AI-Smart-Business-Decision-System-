# AI Smart Business Decision System

BizAI Pro is an AI-powered business intelligence backend built with Spring Boot and an embedded SQLite database. It leverages OpenAI's API to provide inventory forecasting, route optimization, sales analytics, and review sentiment analysis.

## Features
- **Local Embedded Database**: Runs completely standalone via SQLite—**zero Docker required**.
- **Sales Analytics & Inventory Alerts**: Real-time sales calculations and low-stock alerting.
- **AI Forecasting**: Simulates demand forecasting and review sentiment analysis via direct OpenAI API integrations.
- **Data Ingestion**: Python ETL scripts to easily wipe and ingest the Kaggle Online Retail Dataset natively into SQLite.

## Prerequisites
- Java 17+
- Maven
- Python 3.9+ (For data ingestion only)
- Optional: an OpenAI API key (for AI features to work)

## Setup & Running Locally

### 1. Set your OpenAI Key
Set your OpenAI API key in your environment variables so the Spring application can read it:

**Windows PowerShell:**
```powershell
$env:OPENAI_API_KEY="sk-..."
```
**Mac/Linux:**
```bash
export OPENAI_API_KEY="sk-..."
```

### 2. Start the Application
Simply run the included startup script from the root directory. It will compile the Java code and start the embedded server on port 8080.
```powershell
./start.ps1
```
*Alternatively, you can manually run `mvn spring-boot:run`*

The system will automatically initialize the `data/ai_business.db` file and build the schema.

### 3. Load the Dataset (Optional)
The database starts empty. To fill it with products and sales data, run the Python ingestion script:
```powershell
cd ai-service
pip install -r requirements.txt
python ingest_dataset.py
```

## API Endpoints
Once running, you can access the REST APIs at `http://localhost:8080/api/`
- `GET /api/products` - List products
- `GET /api/analytics/top-products` - Sales reports
- `GET /api/inventory/alerts` - Restock alerts

## Deployment
Because this project does not use Docker containers, you can easily deploy it by running `mvn clean package` and uploading the resulting `target/*.jar` file to any standard Java application hosting service (e.g., AWS Elastic Beanstalk, Render, or a basic VPS).
