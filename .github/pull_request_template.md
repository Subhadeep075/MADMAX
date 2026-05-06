## Summary
- What changed?

## Why
- Why this change is needed?

## Scope
- [ ] backend
- [ ] admin-web
- [ ] customer-app
- [ ] docs/chore

## Validation
- [ ] Backend compiles: `cd backend && mvn -DskipTests compile`
- [ ] Admin production build: `cd admin-web && CI=1 npx ng build --configuration production --progress=false`
- [ ] Customer production build: `cd customer-app && CI=1 npx ng build --configuration production --progress=false`

## Screenshots / API notes
- Add screenshots or sample request/response if UI/API changed.

## Checklist
- [ ] No secrets added
- [ ] Updated docs if behavior changed
- [ ] Backward compatibility considered
- [ ] Tested happy path and one failure path

