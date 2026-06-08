# Documentation Index

Welcome to MkSafeNet_Kids documentation! This index helps you find the right guide for your needs.

## 📖 Quick Navigation

### I'm a New Developer
**Goal**: Get the project running locally and understand the codebase

1. **[README.md](../README.md)** (5 min read)
   - Project overview and quick start
   - Project structure
   - Technology stack

2. **[Local Setup Guide](setup-local.md)** (20 min)
   - Step-by-step environment setup
   - Troubleshooting common issues
   - Running the application

3. **[Architecture Overview](architecture.md)** (15 min)
   - How the system works
   - Component interactions
   - Data flow examples

4. **[Backend Developer Guide](backend-developer-guide.md)** or **[Frontend Developer Guide](frontend-developer-guide.md)** (30 min)
   - Depending on your role
   - Adding new features
   - Code organization

### I'm Deploying to Production
**Goal**: Get the application running in production

1. **[Production Setup Guide](setup-production.md)** (10 min)
   - Quick reference for production
   - Environment configuration
   - Deployment checklist

2. **[Deployment Guide](deployment.md)** (30-60 min)
   - Detailed deployment instructions
   - Multiple deployment options (Docker, VPS, Cloud)
   - SSL/TLS, backups, monitoring

3. **[Security & Authentication](security.md)** (20 min)
   - JWT configuration
   - Role-based access control
   - Security best practices

4. **[Database Guide](database.md)** (15 min)
   - Database schema
   - Backup strategy
   - Production database options

### I'm Extending the Backend
**Goal**: Add new API endpoints or features

1. **[API Reference](api-reference.md)** (10 min)
   - See all existing endpoints
   - Request/response examples
   - curl commands for testing

2. **[Backend Developer Guide](backend-developer-guide.md)** (30 min)
   - Adding new endpoints
   - Creating services and entities
   - Testing guidelines

3. **[Database Guide](database.md)** (15 min)
   - If you need new tables
   - Adding relationships
   - Migrations

4. **[Security & Authentication](security.md)** (10 min)
   - Protecting new endpoints
   - Role-based access

### I'm Extending the Frontend
**Goal**: Add new views or components

1. **[Frontend Developer Guide](frontend-developer-guide.md)** (30 min)
   - Creating views and components
   - State management (Pinia)
   - API integration

2. **[API Reference](api-reference.md)** (10 min)
   - Understanding available endpoints
   - Request/response formats
   - Example curl calls

3. **[Architecture Overview](architecture.md)** (10 min)
   - Frontend component structure
   - Data flow
   - Routing

### I'm Integrating APIs
**Goal**: Call backend endpoints from frontend or other clients

1. **[API Reference](api-reference.md)** (20 min)
   - Complete endpoint documentation
   - Request/response examples
   - curl commands
   - Status codes and error handling

2. **[Security & Authentication](security.md)** (15 min)
   - JWT token usage
   - Roles and permissions
   - Error handling (401, 403)

### I'm Testing the Application
**Goal**: Write and run tests

1. **[Testing Guide](testing.md)** (30 min)
   - Unit testing (backend and frontend)
   - Integration testing
   - E2E testing
   - Manual testing checklist

2. **[Local Setup Guide](setup-local.md)** (10 min)
   - Running tests locally
   - IDE setup for debugging

### I'm Troubleshooting an Issue
**Goal**: Fix problems and errors

1. **[Troubleshooting & FAQ](troubleshooting.md)** (10-30 min)
   - Common errors and solutions
   - Backend/frontend issues
   - Port conflicts
   - Environment variable issues

2. **Relevant Guide** (15-30 min)
   - If backend issue: [Backend Developer Guide](backend-developer-guide.md)
   - If frontend issue: [Frontend Developer Guide](frontend-developer-guide.md)
   - If API issue: [API Reference](api-reference.md)
   - If deployment issue: [Deployment Guide](deployment.md)

### I'm Contributing Code
**Goal**: Submit changes following project standards

1. **[Contributing Guidelines](../CONTRIBUTING.md)** (10 min)
   - Branching and PR workflow
   - Code style
   - Testing requirements

2. **[Changelog](../CHANGELOG.md)** (5 min)
   - How to document your changes
   - Versioning scheme
   - Release process

---

## 📚 Complete Documentation List

| Document | Purpose | Read Time | Audience |
|----------|---------|-----------|----------|
| [README.md](../README.md) | Project overview | 5 min | Everyone |
| [CONTRIBUTING.md](../CONTRIBUTING.md) | Contribution guidelines | 10 min | Contributors |
| [CHANGELOG.md](../CHANGELOG.md) | Release notes | 5-10 min | Release managers |
| [architecture.md](architecture.md) | System design | 15 min | Developers |
| [api-reference.md](api-reference.md) | API endpoints | 20 min | API consumers |
| [backend-developer-guide.md](backend-developer-guide.md) | Backend development | 30 min | Java developers |
| [frontend-developer-guide.md](frontend-developer-guide.md) | Frontend development | 30 min | Vue developers |
| [database.md](database.md) | Database schema | 15 min | Backend devs, DBAs |
| [security.md](security.md) | Authentication & security | 20 min | DevOps, Security |
| [setup-local.md](setup-local.md) | Local development | 20 min | All developers |
| [setup-production.md](setup-production.md) | Production setup quick ref | 10 min | DevOps engineers |
| [deployment.md](deployment.md) | Full deployment guide | 30-60 min | DevOps, Release mgrs |
| [testing.md](testing.md) | Testing strategies | 30 min | QA, Developers |
| [troubleshooting.md](troubleshooting.md) | Common issues | 10-30 min | All developers |

---

## 🔍 Search by Topic

### Authentication & Security
- [Security & Authentication](security.md) - JWT, roles, best practices
- [API Reference](api-reference.md#authentication) - `/api/auth` endpoints
- [Backend Developer Guide](backend-developer-guide.md#security) - Implementing auth

### API Development
- [API Reference](api-reference.md) - Complete endpoint docs
- [Backend Developer Guide](backend-developer-guide.md) - Adding endpoints
- [Architecture](architecture.md#backend-architecture) - How endpoints work

### Frontend Development
- [Frontend Developer Guide](frontend-developer-guide.md) - Components, state, routing
- [Architecture](architecture.md#frontend-architecture) - Frontend structure
- [API Reference](api-reference.md) - Calling endpoints

### Database & Data
- [Database Guide](database.md) - Schema, entities, migrations
- [Backend Developer Guide](backend-developer-guide.md#adding-a-new-entity) - Creating tables
- [Architecture](architecture.md#entity-relationship-diagram) - Data model

### Deployment & DevOps
- [Production Setup](setup-production.md) - Quick reference
- [Deployment Guide](deployment.md) - Complete guide
- [Database Guide](database.md#migration-to-production-database) - Production DB
- [Security](security.md#jwt-configuration) - Production settings

### Testing
- [Testing Guide](testing.md) - Unit, integration, E2E
- [Backend Developer Guide](backend-developer-guide.md#testing) - Java tests
- [Frontend Developer Guide](frontend-developer-guide.md#testing) - Vue tests

### Development Environment
- [Local Setup](setup-local.md) - Initial setup
- [Troubleshooting](troubleshooting.md) - Fixing issues
- [Contributing](../CONTRIBUTING.md) - Code standards

---

## ⚡ Common Tasks

### Add a New Backend Endpoint
1. Read [Backend Developer Guide](backend-developer-guide.md#adding-a-new-api-endpoint)
2. Check [API Reference](api-reference.md) for similar endpoints
3. Reference [Security](security.md) if authentication needed
4. Update [API Reference](api-reference.md) with new endpoint docs

### Add a New Frontend View
1. Read [Frontend Developer Guide](frontend-developer-guide.md#creating-a-new-view-component)
2. Check [API Reference](api-reference.md) for endpoints you need
3. Reference [Architecture](architecture.md#frontend-architecture) for structure
4. Test with [Testing Guide](testing.md#frontend-testing)

### Deploy to Production
1. Read [Production Setup](setup-production.md) for quick ref
2. Read [Deployment Guide](deployment.md) for detailed steps
3. Configure [Security](security.md) settings
4. Set up backups from [Database Guide](database.md)

### Write a Test
1. Check [Testing Guide](testing.md) for your test type
2. Review code examples in the guide
3. Run tests with `mvn test` (backend) or `npm test` (frontend)

### Fix an Error
1. Search in [Troubleshooting](troubleshooting.md) for error message
2. Follow solution provided
3. Check relevant guide if needed
4. Verify fix and document if it's a new issue

### Add a Database Table
1. Read [Backend Developer Guide](backend-developer-guide.md#adding-a-new-entity)
2. Reference [Database Guide](database.md) for schema
3. Create JPA entity and repository
4. Restart backend (auto-migration)

### Deploy a New Version
1. Update code and test thoroughly
2. Read [Testing Guide](testing.md) to ensure quality
3. Update [CHANGELOG.md](../CHANGELOG.md)
4. Follow [Contributing](../CONTRIBUTING.md) PR process
5. Follow [Deployment Guide](deployment.md) for production

---

## 💡 Documentation Philosophy

This documentation is designed to be:

- **Searchable**: Use Ctrl+F within documents to find specific topics
- **Linked**: Cross-references help navigate between related topics
- **Practical**: Real examples and step-by-step guides
- **Maintainable**: Easy to update alongside code changes
- **Progressive**: Start simple, dive deeper as needed

---

## 📞 Getting Help

1. **Search this documentation** using Ctrl+F
2. **Check [Troubleshooting](troubleshooting.md)** for common issues
3. **Review [Architecture](architecture.md)** to understand system
4. **Check code comments** in the codebase
5. **Ask in team channels** if still stuck

---

## 🔄 Documentation Feedback

If you find:
- **Errors or outdated info**: Update and commit to git
- **Missing topics**: Add new section
- **Unclear explanations**: Rewrite for clarity
- **Better examples**: Submit improved version

See [Contributing](../CONTRIBUTING.md) for how to contribute.

---

**Documentation Last Updated**: June 8, 2026  
**Coverage**: Backend, Frontend, API, Database, Deployment, Security, Testing, Troubleshooting  
**Total Files**: 13 documentation files + README

