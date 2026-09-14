import streamlit as st
import uuid,requests

API_URL = "http://llm-backend:8080/api/v1/chatStream"
st.set_page_config(page_title="LLM Chat App", page_icon="💬")
st.title("LLM Chat")

providers = { "gemini":"Google Gemini", "openai":"OpenAI ChatGPT", "ollama":"Local LLama" }
models = {
    "gemini":["gemini-3.5-flash-lite","gemini-3.1-flash-lite","gemini-3.8-flash","gemini-3.7-flash","gemini-3.6-flash","gemini-3.5-flash"],
    "openai":["gpt-4.1-nano","gpt-4.1-mini","gpt-5-nano","gpt-5.6-luna","gpt-5-mini","gpt-5.6-terra"],
    "ollama":["gemma2:2b", "llama3.2:3B", "qwen3.5:0.8b","phi4-mini:3.8B","qwen2.5-coder:1.5b"]
}

if "messages" not in st.session_state:
    st.session_state.messages = []
if "session_id" not in st.session_state:
    st.session_state.session_id = str(uuid.uuid4())

def chat_stream(message: str, session_id: str) :
    headers = { "X-Session-ID": session_id }
    payload = { "message": message, "modelProvider": selected_provider,"model":selected_model}
    try:
        with requests.post( API_URL, json=payload, headers=headers, stream=True) as resp:
            if resp.status_code != 200:
                yield f"Backend Error ({resp.status_code}): {resp.text}"
                return
            for chunk in resp.iter_content(chunk_size=None, decode_unicode=True):
                yield chunk.replace("data:","").encode("latin-1").decode("utf-8")[:-2]
    except requests.exceptions.RequestException as e:
        yield f"Unexpected Error: {str(e)}"

chat_container = st.container()

with chat_container:
    for msg in st.session_state.messages:
        with st.chat_message(msg["role"]):
            st.markdown(msg["content"])

with st.bottom:
    col_input,col_provider,col_model  = st.columns([9, 4.2, 4.8], vertical_alignment="center")
    with col_input:
        user_prompt = st.chat_input("Type your message here...")
    with col_provider:
        selected_provider = st.selectbox( "Model Provider", options=providers.keys() ,format_func=lambda x : providers[x],label_visibility="collapsed")
    with col_model:
        selected_model = st.selectbox( "Model", options=models[selected_provider],label_visibility="collapsed")

if user_prompt:
    st.session_state.messages.append({"role": "user", "content": user_prompt})
    with chat_container:
        with st.chat_message("user"):
            st.markdown(user_prompt)
        with st.chat_message("assistant"):
            response = st.write_stream(chat_stream(user_prompt, st.session_state.session_id))
    st.session_state.messages.append({"role": "assistant", "content": response})
