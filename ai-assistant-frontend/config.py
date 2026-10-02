import os


REDIS_HOST = os.getenv("REDIS_HOST")
REDIS_PORT = int(os.getenv("REDIS_PORT"))
BACKEND_URL = os.getenv("BACKEND_URL")

CHAT_API_URL = f"{BACKEND_URL}/chatStream"
FILE_UPLOAD_URL = f"{BACKEND_URL}/uploadFile"
FILE_DELETE_URL = f"{BACKEND_URL}/deleteFile"
DELETE_SESSION_URL = f"{BACKEND_URL}/deleteSession"

PAGE_CONFIG = {
    "page_title": "AI Assistant",
    "page_icon": "💬",
    "layout": "wide",
}

PROVIDERS = {
    "GEMINI": "Google Gemini",
    "OPENAI": "OpenAI ChatGPT",
    "OLLAMA": "Open-source Models",
}

MODELS = {
    "GEMINI": [
        "gemini-3.5-flash-lite",
        "gemini-3.1-flash-lite",
        "gemini-3.8-flash",
        "gemini-3.7-flash",
        "gemini-3.6-flash",
        "gemini-3.5-flash",
    ],
    "OPENAI": [
        "gpt-4.1-nano",
        "gpt-4.1-mini",
        "gpt-5-nano",
        "gpt-5.6-luna",
        "gpt-5-mini",
        "gpt-5.6-terra",
    ],
    "OLLAMA": [
        "gemma2:2b",
        "llama3.2:3B",
        "qwen3.5:0.8b",
        "phi4-mini:3.8B",
        "qwen2.5-coder:1.5b",
    ],
}
