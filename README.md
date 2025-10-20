<img width="1911" height="975" alt="Screenshot 2025-10-20 231547" src="https://github.com/user-attachments/assets/24ccd451-8d9f-44ed-9863-b2cdc3de1889" />


-----------------------------------------------------------------------------------------------------------------------------------------


<img width="1856" height="279" alt="Screenshot 2025-10-20 231634" src="https://github.com/user-attachments/assets/5cd58cc2-c8e7-4504-9dd3-b6f13dc2da58" />


-----------------------------------------------------------------------------------------------------------------------------------------

<img width="1017" height="817" alt="Screenshot 2025-10-20 231655" src="https://github.com/user-attachments/assets/1dc848a7-68a2-46d7-bb65-acf69d711969" />


-----------------------------------------------------------------------------------------------------------------------------------------


# Task Manager Frontend

A modern React 19 frontend application built with TypeScript and Ant Design for managing tasks and executing commands.

## Features

- **Task Management**: Create, read, update, and delete tasks
- **Task Execution**: Execute tasks and view command outputs
- **Search & Filter**: Search tasks by name, owner, or command
- **Execution History**: View detailed execution history with timestamps and outputs
- **Responsive Design**: Mobile-friendly interface using Ant Design
- **Accessibility**: WCAG compliant with proper ARIA labels and keyboard navigation

## Technology Stack

- **React 19**: Latest React with modern features
- **TypeScript**: Type-safe development
- **Ant Design**: Professional UI component library
- **Vite**: Fast build tool and development server
- **Axios**: HTTP client for API communication
- **React Router**: Client-side routing

## Prerequisites

- Node.js 18+ 
- npm or yarn
- Backend API running on http://localhost:8080

## Installation

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm run dev
```

3. Open http://localhost:3000 in your browser

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build

## API Integration

The frontend communicates with the Spring Boot backend API:

- **GET /tasks** - Fetch all tasks
- **GET /tasks/{id}** - Fetch task by ID
- **POST /tasks** - Create new task
- **PUT /tasks/{id}** - Update task
- **DELETE /tasks/{id}** - Delete task
- **PUT /tasks/{id}/execute** - Execute task

## Accessibility Features

- Semantic HTML structure
- ARIA labels for screen readers
- Keyboard navigation support
- High contrast focus indicators
- Screen reader friendly content
- Proper heading hierarchy

## Security

- Command validation on frontend
- Input sanitization
- XSS protection through React
- CSRF protection via Axios configuration

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+
