import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './style.css';

type UserProfile = { id: string; email: string; displayName: string };
type FeedPost = { id: string; content: string; createdAt: string; authorName: string };
type AuthenticationMode = 'login' | 'register';
type AuthenticationForm = { email: string; password: string; displayName: string };

// 所有請求攜帶 HttpOnly auth_token cookie；前端不會也不應讀取 token 本身。
async function requestApi(endpoint: string, requestOptions: RequestInit = {}) {
  const response = await fetch('/api' + endpoint, {
    ...requestOptions,
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...(requestOptions.headers || {}) },
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({ message: '請求失敗' }));
    throw new Error(errorBody.message || '請求失敗');
  }

  return response.status === 204 ? null : response.json();
}

function App() {
  const [currentUser, setCurrentUser] = useState<UserProfile | null>(null);
  const [feedPosts, setFeedPosts] = useState<FeedPost[]>([]);
  const [authenticationMode, setAuthenticationMode] = useState<AuthenticationMode>('login');
  const [authenticationForm, setAuthenticationForm] = useState<AuthenticationForm>({
    email: '',
    password: '',
    displayName: '',
  });
  const [postContent, setPostContent] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const loadFeed = async () => {
    try {
      setFeedPosts(await requestApi('/feed'));
    } catch (caughtError) {
      setErrorMessage((caughtError as Error).message);
    }
  };

  useEffect(() => {
    const loadAuthenticatedSession = async () => {
      try {
        const authenticatedUser = await requestApi('/auth/me');
        setCurrentUser(authenticatedUser);
        await loadFeed();
      } catch {
        // 未登入時只顯示登入畫面，且不請求受保護的 Feed API。
      }
    };

    loadAuthenticatedSession();
  }, []);

  const handleAuthenticationSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setErrorMessage('');

    try {
      const authenticationResponse = await requestApi(
        '/auth/' + (authenticationMode === 'login' ? 'login' : 'register'),
        {
          method: 'POST',
          body: JSON.stringify(authenticationForm),
        },
      );
      setCurrentUser(authenticationResponse);
      await loadFeed();
    } catch (caughtError) {
      setErrorMessage((caughtError as Error).message);
    }
  };

  const handlePostSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    try {
      await requestApi('/posts', {
        method: 'POST',
        body: JSON.stringify({ content: postContent }),
      });
      setPostContent('');
      await loadFeed();
    } catch (caughtError) {
      setErrorMessage((caughtError as Error).message);
    }
  };

  const handleLogout = async () => {
    await requestApi('/auth/logout', { method: 'POST' });
    setCurrentUser(null);
  };

  if (!currentUser) {

    return (
      <main>
        <section className="hero">
          <p>NEWS FEED · 第一階段 DEMO</p>
          <h1>登入後，發一則貼文。</h1>
          <span>PostgreSQL × Redis × RabbitMQ × Java</span>
        </section>
        <section className="auth">
          <div className="tabs">
            <button
              onClick={() => setAuthenticationMode('login')}
              className={authenticationMode === 'login' ? 'on' : ''}
            >
              登入
            </button>
            <button
              onClick={() => setAuthenticationMode('register')}
              className={authenticationMode === 'register' ? 'on' : ''}
            >
              註冊
            </button>
          </div>
          <form onSubmit={handleAuthenticationSubmit}>
            {authenticationMode === 'register' && (
              <input
                placeholder="顯示名稱"
                required
                maxLength={40}
                value={authenticationForm.displayName}
                onChange={(event) =>
                  setAuthenticationForm({
                    ...authenticationForm,
                    displayName: event.target.value,
                  })
                }
              />
            )}
            <input
              type="email"
              placeholder="Email"
              required
              value={authenticationForm.email}
              onChange={(event) =>
                setAuthenticationForm({ ...authenticationForm, email: event.target.value })
              }
            />
            <input
              type="password"
              placeholder="密碼（至少 8 碼）"
              required
              minLength={8}
              value={authenticationForm.password}
              onChange={(event) =>
                setAuthenticationForm({ ...authenticationForm, password: event.target.value })
              }
            />
            <button className="primary">
              {authenticationMode === 'login' ? '登入' : '建立帳號'}
            </button>
          </form>
          {errorMessage && <p className="error">{errorMessage}</p>}
        </section>
      </main>
    );
  }

  return (
    <main>
      <header>
        <div>
          <p>NEWS FEED</p>
          <h1>嗨，{currentUser.displayName}</h1>
        </div>
        <button onClick={handleLogout}>登出</button>
      </header>
      <section className="composer">
        <form onSubmit={handlePostSubmit}>
          <textarea
            placeholder="你在想什麼？"
            value={postContent}
            maxLength={500}
            required
            onChange={(event) => setPostContent(event.target.value)}
          />
          <div>
            <small>{postContent.length}/500</small>
            <button className="primary">發佈貼文</button>
          </div>
        </form>
      </section>
      <section className="feed">
        <h2>最新動態</h2>
        {feedPosts.map((feedPost) => (
          <article key={feedPost.id}>
            <b>{feedPost.authorName}</b>
            <time>{new Date(feedPost.createdAt).toLocaleString('zh-TW')}</time>
            <p>{feedPost.content}</p>
          </article>
        ))}
        {feedPosts.length === 0 && <p className="empty">還沒有貼文，成為第一位發文的人吧。</p>}
      </section>
      {errorMessage && <p className="error">{errorMessage}</p>}
    </main>
  );
}

createRoot(document.getElementById('root')!).render(<App />);
