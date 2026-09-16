---
name: feature-code-review
description: Review a feature diff fail-closed against repository standards, the complete approved specification, and test/QA quality. Use before declaring a feature complete, when reviewing a branch or PR, or when asked whether requirements and tests are fully covered.
---

# Feature Code Review

Review only; do not change code unless the user asks for fixes.

## Establish the review boundary

1. Resolve a fixed base commit or merge-base. Never review only the latest commit when the feature spans several commits.
2. Read `AGENTS.md`, `requirements-scope.txt`, every scoped requirements/acceptance file, and the originating issue or user plan in full.
3. Inspect the complete `base...HEAD` diff and its commits. Treat requirements omitted from the implementation as findings, not as out of scope.

## Build the acceptance matrix first

Create one row for every approved `TEST_*` with:

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
- Reject shared-order E2E tests; each test must pass alone and one failure must not skip unrelated tests.
- Reject wall-clock-dependent fixtures unless time is explicitly fixed.
- Require negative CSRF/auth tests for protected mutations.
- Test production configuration directly, not a test override or fallback value.
- Check public/protected route behavior, role-specific UI actions, profile flows, and REST success/error contracts when in scope.
- Reject assertions that prove only setup or stored data while missing the required behavior after a state change.

## Execute the gates

Run `./verify.sh`. Also execute each changed E2E test independently when shared state could hide ordering dependencies. Record every command and result.

## Verdict

Report findings first, ordered by severity, under `Standards`, `Spec`, and `Test / QA`, with exact file and line references. Then report matrix coverage and commands.

Never report `clean`, `complete`, or `all requirements implemented` if:

- any matrix row is partial, missing, non-deterministic, order-dependent, or unknown;
- a required command failed or was not run;
- an unresolved finding contradicts a requirement.

After fixes, repeat all three lanes against the complete fixed-point diff. Do not merely recheck the edited lines.
