import { createElement } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { router } from './generated/routes';

const container = document.getElementById('outlet');
const root = createRoot(container!);
root.render(createElement(RouterProvider, { router }));