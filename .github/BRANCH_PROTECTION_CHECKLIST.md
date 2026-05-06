# Branch Protection Checklist (`main`)

Apply these settings in GitHub:

Repository -> `Settings` -> `Branches` -> `Add branch protection rule`

## Rule target
- Branch name pattern: `main`

## Recommended toggles
- `Require a pull request before merging`: ON
- `Require approvals`: ON (minimum 1)
- `Dismiss stale pull request approvals when new commits are pushed`: ON
- `Require review from code owners`: ON (if `CODEOWNERS` added later)
- `Require status checks to pass before merging`: ON
- Required checks:
  - `backend-build`
  - `admin-web-build`
  - `customer-app-build`
- `Require branches to be up to date before merging`: ON
- `Require conversation resolution before merging`: ON
- `Require signed commits`: Optional
- `Require linear history`: ON
- `Allow force pushes`: OFF
- `Allow deletions`: OFF

## Admin safety
- `Do not allow bypassing the above settings`: ON (recommended for production)

## Merge strategy (Repository -> Settings -> General)
- Allow merge commits: OFF
- Allow squash merging: ON
- Allow rebase merging: ON
- Auto-delete head branches: ON

## Notes
- For solo development, you can temporarily keep bypass ON for admins while bootstrapping.
- Before public production, lock down bypass to enforce review + CI discipline.
