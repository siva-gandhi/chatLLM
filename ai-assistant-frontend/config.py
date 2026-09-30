REDIS_HOST = "redis"
REDIS_PORT = 6379

CHAT_API_URL = "http://ai-backend:8080/api/v1/chatStream"
FILE_UPLOAD_URL = "http://ai-backend:8080/api/v1/uploadFile"
FILE_DELETE_URL = "http://ai-backend:8080/api/v1/deleteFile"

PAGE_CONFIG = {
    "page_title": "AI Assistant",
    "page_icon": "💬",
    "layout": "wide",
}

PROVIDERS = {
    "gemini": "Google Gemini",
    "openai": "OpenAI ChatGPT",
    "ollama": "Open-source Models",
}

MODELS = {
    "gemini": [
        "gemini-3.5-flash-lite",
        "gemini-3.1-flash-lite",
        "gemini-3.8-flash",
        "gemini-3.7-flash",
        "gemini-3.6-flash",
        "gemini-3.5-flash",
    ],
    "openai": [
        "gpt-4.1-nano",
        "gpt-4.1-mini",
        "gpt-5-nano",
        "gpt-5.6-luna",
        "gpt-5-mini",
        "gpt-5.6-terra",
    ],
    "ollama": [
        "gemma2:2b",
        "llama3.2:3B",
        "qwen3.5:0.8b",
        "phi4-mini:3.8B",
        "qwen2.5-coder:1.5b",
    ],
}
