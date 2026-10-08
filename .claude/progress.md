# Build progress — Notes v2

Maintained by the android-app-builder skill. On resume, continue from the first task that is not `done`.
Do not edit by hand unless you also edit docs/TASKS.md.

| Task | Status | Gate summary | Commit |
|---|---|---|---|
| C1–C16 scaffold | done | build ✓ static ✓ tests ✓ rules ✓ dead-code skipped · ratio 0.16 | build: scaffold project |
| D0 app shell | done | flavors + deep-link hosts, Firebase/Crashlytics (placeholder google-services.json) | feat(notes) |
| noteslist | done | build ✓ static ✓ tests ✓ rules ✓ dead-code 0 · ratio 0.40 | feat(notes) |
| notedetail | done | build ✓ static ✓ tests ✓ rules ✓ dead-code 0 · ratio 0.40 (analytics, delete/undo, deep links) | feat(notes) |
| noteeditor | done | build ✓ static ✓ tests ✓ rules ✓ dead-code 0 · ratio 0.40 (create/edit, FCM + rationale card) | feat(noteeditor) |
| config (app gate) | done | build ✓ static ✓ tests ✓ rules ✓ dead-code 0 · ratio 0.41 | feat(config) |
| E1 hardening | done | pins for jsonplaceholder (WE1 + GTS R4, backups GTS R1 + ISRG X1, 2026-10-08) | fix(noteslist) |
| E2 assembleProdRelease | done | R8 OK, 8.3 MB | |
| E2b smoke test | in progress | release on API 36 emulator: list, create (found + fixed off-screen new note), permission, detail, edit, delete, undo ✓; still to do: offline, tr, deep link | |
