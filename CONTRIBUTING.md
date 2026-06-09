# Contributing to MkSafeNet_Kids

Thanks for your interest in contributing! This document describes the preferred workflow, code style, and testing expectations for the project.

Branching and PRs
- Use feature branches branching from `main` or `dev` (if present). Name branches like `feature/<short-desc>` or `fix/<short-desc>`.
- Open a Pull Request (PR) targeting `main` (or `dev`). Provide a clear title and description.
- Include screenshots or recordings for UI changes and describe API changes.

Commits
- Make small focused commits with meaningful messages.
- Use present-tense, imperative style: `Add login endpoint` not `Added`.

Code style
- Java (backend): follow standard Spring Boot conventions. Use Lombok annotations when convenient.
- JavaScript (frontend): use ES modules, prefer const/let, and follow existing project style.
- Keep formatting consistent. We recommend running an auto-formatter in your editor (Prettier for frontend, built-in IDE formatting for Java).

Running tests
- Backend: run `mvn -q test` from `backend` directory.
- Frontend: run `npm test` if tests are present; otherwise use `npm run dev` and manual checks.

PR Review
- At least one reviewer should approve. Ensure all CI checks pass.
- Squash or rebase as needed before merging to keep a clean history.

Adding features
- Update relevant docs under `docs/` and README. Add API docs for new endpoints in `docs/api-reference.md`.

Security
- Do not commit secrets. Use environment variables for sensitive values (JWT secret, DB credentials). See `backend/src/main/resources/application.properties` for defaults.

Thanks for contributing!

