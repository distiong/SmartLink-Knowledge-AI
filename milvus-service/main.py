import os
import uvicorn
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import threading
import numpy as np
import json

app = FastAPI(title="Milvus Service")

VECTOR_DIM = 384

embedding_model = None
model_loaded = False
model_loading = False

# 内存向量存储
vector_store = {}  # {id: {"document_id": int, "chunk": str, "vector": List[float]}}

class EmbedRequest(BaseModel):
    text: str

class BatchEmbedRequest(BaseModel):
    texts: List[str]

class InsertRequest(BaseModel):
    document_id: int
    chunks: List[str]
    vectors: List[List[float]]

class SearchRequest(BaseModel):
    query_vector: List[float]
    top_k: int = 5

class SearchResult(BaseModel):
    text: str
    score: float
    document_id: int

def load_model_async():
    global embedding_model, model_loaded, model_loading
    if model_loading:
        return
    model_loading = True
    try:
        from sentence_transformers import SentenceTransformer
        print("Loading embedding model...")
        embedding_model = SentenceTransformer('all-MiniLM-L6-v2')
        model_loaded = True
        print("Embedding model loaded successfully")
    except Exception as e:
        print(f"Warning: Could not load embedding model: {e}")
        model_loaded = True
    finally:
        model_loading = False

def get_embedding_model():
    global embedding_model, model_loaded
    if not model_loaded and not model_loading:
        thread = threading.Thread(target=load_model_async)
        thread.start()
    return embedding_model

def cosine_similarity(vec1, vec2):
    """计算余弦相似度"""
    vec1 = np.array(vec1)
    vec2 = np.array(vec2)
    dot_product = np.dot(vec1, vec2)
    norm1 = np.linalg.norm(vec1)
    norm2 = np.linalg.norm(vec2)
    if norm1 == 0 or norm2 == 0:
        return 0
    return dot_product / (norm1 * norm2)

@app.on_event("startup")
async def startup():
    print("Starting Milvus Service...")
    thread = threading.Thread(target=load_model_async)
    thread.start()

@app.post("/embed")
async def embed_text(request: EmbedRequest):
    try:
        model = get_embedding_model()
        if model is None:
            raise HTTPException(status_code=503, detail="Embedding model still loading")
        embedding = model.encode(request.text)
        return {"embedding": embedding.tolist()}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/batch_embed")
async def batch_embed_text(request: BatchEmbedRequest):
    try:
        model = get_embedding_model()
        if model is None:
            raise HTTPException(status_code=503, detail="Embedding model still loading")
        embeddings = model.encode(request.texts)
        return {"embeddings": embeddings.tolist()}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/insert")
async def insert_vectors(request: InsertRequest):
    try:
        global vector_store
        for i, (chunk, vector) in enumerate(zip(request.chunks, request.vectors)):
            key = f"{request.document_id}_{i}"
            vector_store[key] = {
                "document_id": request.document_id,
                "chunk": chunk,
                "vector": vector
            }
        print(f"Inserted {len(request.chunks)} vectors for document {request.document_id}")
        return {"status": "success", "count": len(request.chunks)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/search")
async def search_vectors(request: SearchRequest):
    try:
        global vector_store
        
        if not vector_store:
            return {"results": []}
        
        # 计算所有向量与查询向量的相似度
        results = []
        for key, data in vector_store.items():
            score = cosine_similarity(request.query_vector, data["vector"])
            results.append({
                "text": data["chunk"],
                "score": float(score),
                "document_id": data["document_id"]
            })
        
        # 按相似度排序，返回top_k
        results.sort(key=lambda x: x["score"], reverse=True)
        results = results[:request.top_k]
        
        return {"results": results}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.delete("/delete/{document_id}")
async def delete_by_document_id(document_id: int):
    try:
        global vector_store
        keys_to_delete = [k for k, v in vector_store.items() if v["document_id"] == document_id]
        for key in keys_to_delete:
            del vector_store[key]
        return {"status": "success", "deleted": len(keys_to_delete)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
async def health():
    model = get_embedding_model()
    return {
        "status": "ok",
        "model_loaded": model is not None,
        "vector_count": len(vector_store)
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8081)
