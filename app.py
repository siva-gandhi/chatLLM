import streamlit as st
import uuid
import requests
import logging
from datetime import datetime

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] - %(message)s",
    handlers=[logging.StreamHandler()]
)
logger = logging.getLogger(__name__)

API_URL = "http://llm-backend:8080/api/v1/chatStream"
FILE_UPLOAD_URL = "http://llm-backend:8080/api/v1/uploadFile"
FILE_DELETE_URL = "http://llm-backend:8080/api/v1/deleteFile"

st.set_page_config(page_title="LLM Chat App", page_icon="💬", layout="wide")

providers = {
    "gemini": "Google Gemini",
    "openai": "OpenAI ChatGPT",
    "ollama": "Open-source Models"
}

models = {
    "gemini": ["gemini-3.5-flash-lite", "gemini-3.1-flash-lite", "gemini-3.8-flash", "gemini-3.7-flash", "gemini-3.6-flash", "gemini-3.5-flash"],
    "openai": ["gpt-4.1-nano", "gpt-4.1-mini", "gpt-5-nano", "gpt-5.6-luna", "gpt-5-mini", "gpt-5.6-terra"],
    "ollama": ["gemma2:2b", "llama3.2:3B", "qwen3.5:0.8b", "phi4-mini:3.8B", "qwen2.5-coder:1.5b"]
}

# --- Session State Initialization ---
if "messages" not in st.session_state:
    st.session_state.messages = []
if "session_id" not in st.session_state:
    st.session_state.session_id = str(uuid.uuid4())
if "uploaded_files" not in st.session_state:
    st.session_state.uploaded_files = set()
if "past_chats" not in st.session_state:
    st.session_state.past_chats = {}  # Format: {session_id: {"title": str, "messages": list, "timestamp": str}}


# --- Archive & State Helpers ---
def save_current_chat_to_history():
    """Saves the active session messages into the past_chats archive."""
    if st.session_state.messages:
        # Generate title from the first user prompt or fallback
        first_user_msg = next((m["content"] for m in st.session_state.messages if m["role"] == "user"), "Conversation")
        title = (first_user_msg[:32] + "...") if len(first_user_msg) > 32 else first_user_msg

        st.session_state.past_chats[st.session_state.session_id] = {
            "id": st.session_state.session_id,
            "title": title,
            "messages": list(st.session_state.messages),
            "timestamp": datetime.now().strftime("%b %d, %H:%M"),
            "provider" : st.session_state.selected_provider
        }


def session_reset():
    """Archives current context, cleans backend files, and initializes a new session."""
    save_current_chat_to_history()

    for file_id in st.session_state.uploaded_files:
        delete_file_from_backend(st.session_state.session_id, file_id)

    st.session_state.messages = []
    st.session_state.session_id = str(uuid.uuid4())
    st.session_state.uploaded_files = set()
    handle_file_change()
    st.toast("Saved previous chat and reset session.", icon="💾")

def load_chat(target_session_id: str):
    """Saves current state and switches to an archived session."""
    save_current_chat_to_history()

    target_chat = st.session_state.past_chats.get(target_session_id)
    if target_chat:
        st.session_state.session_id = target_chat["id"]
        st.session_state.messages = list(target_chat["messages"])
        st.session_state.uploaded_files = set()
        st.session_state.selected_provider = target_chat["provider"]


# --- Backend API Handlers ---
def upload_file_to_backend(file, session_id: str):
    headers = {"X-Session-ID": session_id, "fileId": file.file_id}
    files = {"file": (file.name, file.getvalue(), file.type)}
    try:
        response = requests.post(FILE_UPLOAD_URL, headers=headers, files=files)
        response.raise_for_status()
        return True, "Success"
    except requests.exceptions.RequestException as e:
        return False, str(e)


def delete_file_from_backend(session_id: str, file_id: str):
    headers = {"X-Session-ID": session_id, "fileId": file_id}
    try:
        response = requests.delete(FILE_DELETE_URL, headers=headers)
        response.raise_for_status()
        return True, "Success"
    except requests.exceptions.RequestException as e:
        return False, str(e)


def chat_stream(message: str, session_id: str):
    headers = {"X-Session-ID": session_id}
    payload = {"message": message, "modelProvider": st.session_state.selected_provider, "model": st.session_state.selected_model}
    try:
        with requests.post(API_URL, json=payload, headers=headers, stream=True) as resp:
            if resp.status_code != 200:
                yield f"Backend Error ({resp.status_code}): {resp.text}"
                return
            for chunk in resp.iter_content(chunk_size=None, decode_unicode=True):
                yield chunk.replace("data:", "").encode("latin-1").decode("utf-8")[:-2]
    except requests.exceptions.RequestException as e:
        yield f"Unexpected Error: {str(e)}"


def handle_file_change():
    current_files = st.session_state.get("file_uploader_widget") or []
    for removed_file_id in (st.session_state.uploaded_files - {f.file_id for f in current_files}):
        success, error_msg = delete_file_from_backend(st.session_state.session_id, removed_file_id)
        if success:
            st.session_state.uploaded_files.remove(removed_file_id)
        else:
            st.error(f"Failed to remove file from backend: {error_msg}")
    for added_file in current_files:
        if added_file.file_id not in st.session_state.uploaded_files:
            success, error_msg = upload_file_to_backend(added_file, st.session_state.session_id)
            if success:
                st.session_state.uploaded_files.add(added_file.file_id)
                st.toast("File upload successful!", icon="✅")
            else:
                st.error(f"Failed to upload {added_file.name}: {error_msg}")


# --- Sidebar: Past Chats & History Management ---
with st.sidebar:
    st.title("Chat Management")
    if st.button("➕ New Chat", use_container_width=True, type="primary"):
        session_reset()
        st.rerun()

    st.subheader("Past Conversations")

    if not st.session_state.past_chats:
        st.caption("No archived conversations yet. When you reset or switch sessions, chats will appear here.")
    else:
        # Display sessions in reverse chronological order
        for sid, chat in reversed(list(st.session_state.past_chats.items())):
            is_active = (sid == st.session_state.session_id)
            col_chat, col_del = st.columns([0.82, 0.18])

            with col_chat:
                button_label = f"{'🟢 ' if is_active else '💬 '}{chat['title']}"
                if st.button(
                    button_label,
                    key=f"load_{sid}",
                    use_container_width=True,
                    help=f"Saved: {chat['timestamp']}"
                ):
                    load_chat(sid)
                    st.rerun()

            with col_del:
                if st.button("🗑️", key=f"del_{sid}", help="Delete this archived chat"):
                    del st.session_state.past_chats[sid]
                    st.rerun()

        st.divider()
        if st.button("Clear All History", use_container_width=True):
            st.session_state.past_chats.clear()
            st.rerun()


# --- Main Chat UI ---
st.title("LLM Chat")

chat_container = st.container()

with chat_container:
    for msg in st.session_state.messages:
        with st.chat_message(msg["role"]):
            st.markdown(msg["content"])

with st.bottom:
    col_provider, col_model, col_file = st.columns([1.5, 1.5, 4], vertical_alignment="bottom")

    with col_provider:
        st.selectbox(
            "Model Provider",
            options=providers.keys(),
            format_func=lambda x: providers[x],
            label_visibility="collapsed",
            on_change=session_reset,
            key="selected_provider"
        )
    with col_model:
        st.selectbox(
            "Model",
            options=models[st.session_state.selected_provider],
            label_visibility="collapsed",
            key="selected_model"
        )
    with col_file:
        st.file_uploader(
            "Upload context file",
            accept_multiple_files=True,
            key="file_uploader_widget",
            label_visibility="collapsed",
            on_change=handle_file_change
        )
    user_prompt = st.chat_input("Type your message here...")

if user_prompt:
    st.session_state.messages.append({"role": "user", "content": user_prompt})
    with chat_container:
        with st.chat_message("user"):
            st.markdown(user_prompt)
        with st.chat_message("assistant"):
            chat_response = st.write_stream(chat_stream(user_prompt, st.session_state.session_id))

    st.session_state.messages.append({"role": "assistant", "content": chat_response})
    # Keep the archived snapshot in sync after every turn
    save_current_chat_to_history()