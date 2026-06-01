import { createRoot } from 'react-dom/client';
import { initMocks } from './mocks/init';
import App from './app/App.tsx';
import './styles/index.css';

initMocks().then(() => {
  createRoot(document.getElementById('root')!).render(<App />);
});
