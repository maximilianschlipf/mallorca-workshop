---
name: feature-code-review
description: Review a feature diff fail-closed against repository standards, the complete approved specification, and test/QA quality. Use before declaring a feature complete, when reviewing a branch or PR, or when asked whether requirements and tests are fully covered.
---

# Feature Code Review

Review only; do not change code unless the user asks for fixes.

## Establish the review boundary

1. Use the user-specified base, or otherwise the merge-base with the default branch. Never review only the latest commit when the feature spans several commits.
2. Derive the feature scope from the originating issue or user request and the complete diff. Include every affected requirement, story, and acceptance test. `implemented-requirements.txt` is only the cumulative product register; never use it as the feature scope.
3. Read `AGENTS.md`, the originating issue or user request, and every in-scope requirements/acceptance file in full. Active means status `approved`, `implemented`, or `verified`.
4. Inspect all commits since the base plus staged, unstaged, and untracked changes. Treat in-scope requirements omitted from the implementation as findings.

## Build the acceptance matrix first

Create one row for every active in-scope `TEST_*` with:

| ID | Complete scenario | Executable test | Product path/layer | Assertions cover all steps | Standalone/deterministic | Verdict |
|---|---|---|---|---|---|---|

A `verifies:` comment is only a pointer. Read the test and product code. Mark a row incomplete when any scenario step, state transition, negative case, or observable result is missing. `unknown` fails the review.

## Run three review lanes

Use isolated reviewers in parallel when the host supports them; otherwise run the lanes sequentially with separate passes.

### Standards

- Check all repository instructions and established local patterns.
- Check security, validation, error handling, accessibility, and unnecessary complexity.
- Report only actionable regressions introduced by the feature.

### Specification

- Trace every requirement and story through API/UI, domain behavior, persistence, and cleanup effects.
- Compare exact contracts: authorization, status codes, response fields, errors, concurrency, and historical data.
- Do not infer completion from green tests.

### Test / QA

- Require executable evidence for every acceptance row.
- Reject shared-order E2E tests. Run every new or changed E2E test case alone; one failure must not skip unrelated tests.
- Reject wall-clock-dependent fixtures unless time is explicitly fixed.
- Require negative CSRF/auth tests for protected mutations in scope.
- When a requirement concerns configuration, test its production binding directly. Infrastructure-only E2E overrides such as ports, temporary data, and fixtures are allowed.
- Check public/protected route behavior, role-specific UI actions, profile flows, and REST success/error contracts when in scope.
- Reject assertions that prove only setup or stored data while missing the required behavior after a state change.

## Execute the gates

After the review, run `./verify.sh` once against the exact reviewed tree. Record its result and the failing step; steps not reached are `NOT RUN`. After fixes, repeat the complete review and gate.

## Verdict

Report findings first, ordered by severity, under `Standards`, `Spec`, and `Test / QA`, with exact file and line references. Then report matrix coverage and commands.

Never report `clean`, `complete`, or `all requirements implemented` if:

- any matrix row is partial, missing, non-deterministic, order-dependent, or unknown;
- a required command failed or was not run;
- an unresolved finding contradicts a requirement.

Do not merely recheck edited lines after fixes.
