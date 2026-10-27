# GadgetGo engineering instructions

- Inspect the existing code before editing. Preserve working functionality and avoid unnecessary rewrites.
- Use Java 21, Spring Boot, React, JavaScript, and PostgreSQL.
- Follow the established architecture and clean layering: controllers stay thin, business logic belongs in services, and repositories handle persistence. Change this only with a documented reason.
- Apply single-responsibility principles and Core Java object-oriented concepts where they fit.
- Use DTOs for future business API request and response contracts.
- Validate inputs and handle errors consistently. Use transactions for operations that must succeed or fail together.
- Enforce authorization on the backend when security is introduced.
- Never commit passwords, tokens, API keys, or other secrets.
- Avoid duplicated code and unnecessary dependencies. Make incremental changes.
- Add meaningful tests and run relevant checks for changes.
- Explain what was changed and what was actually verified; do not claim checks that were not run successfully.
