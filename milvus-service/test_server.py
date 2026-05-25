import uvicorn
from fastapi import FastAPI

app = FastAPI()

@app.get("/test")
async def test():
    return {"status": "ok"}

@app.get("/health")
async def health():
    return {"status": "ok", "model_loaded": False, "milvus_connected": False}

if __name__ == "__main__":
    print("Starting test server on port 8081...")
    uvicorn.run(app, host="0.0.0.0", port=8081)
