# Tasks
## Stage C — scaffold
- [x] C1 Preflight                                   (done in intake: ok, platform 37)
- [x] C2 Resolve versions (with: firebase, startup, analytics, adjust, appsflyer)
- [x] C3 Gradle skeleton + wrapper + git init        check: ./gradlew help
- [x] C4 Version catalog                             check: ./gradlew help
- [x] C5 Convention plugins                          check: build-logic builds
- [x] C6 Quality tooling                             check: detekt ktlintCheck
- [x] C7 :core:common                               gate: core/common
- [x] C8 :core:designsystem (M3 default tokens)      gate: core/designsystem
- [x] C9 :core:i18n (strings_common en + tr)         gate: core/i18n
- [x] C10 :core:model                                gate: core/model
- [x] C11 :core:ui                                   gate: core/ui
- [x] C12 :core:network                              gate: core/network
- [x] C13 :core:data + :core:database                gate: core/data core/database
- [x] C14 :core:testing                              gate: core/testing
- [x] C15 CI workflows
- [x] C16 Scaffold check (GATE_SKIP_DEAD_CODE=1) → commit "build: scaffold project"

## Stage D — features
### D0 app shell (with noteslist)
- [x] D0 Render app module, flavors (deep-link hosts), locales en/tr, Firebase + Crashlytics
        (placeholder google-services.json, CrashReportingTree)                          gate: app
### noteslist
- [x] noteslist-1 Model: Note                                          gate: core/model
- [x] noteslist-2 Remote: NoteDto, NotesApi, mapper + tests            gate: core/network
- [x] noteslist-3 Local: NoteEntity, NoteDao (mergeRemote), AppDatabase + tests   gate: core/database
- [x] noteslist-4 Repository: observeNotes, refresh + fake + tests     gate: core/data
- [x] noteslist-6 ViewModel + UiState + mapper + tests (refresh error) gate: feature/noteslist
- [x] noteslist-7 UI: Route, Screen, NoteListItem, previews, UI tests  gate: feature/noteslist
- [x] noteslist-9 Navigation wiring                                    gate: app
- [x] noteslist-10 Strings (en, tr)                                    gate: core/i18n
- [x] noteslist-done Full gate + dead code + auto-commit
### notedetail
- [x] notedetail-4 Repository: observeNote(id), deleteNote, restoreNote, recentlyDeleted + tests   gate: core/data
- [x] notedetail-5 :core:analytics + Firebase/Adjust/AppsFlyer providers   gate: core/analytics
- [x] notedetail-6 ViewModel (assisted noteId) + UiState + tests (note_opened, delete flow)   gate: feature/notedetail
- [x] notedetail-7 UI: Route, Screen, delete dialog, previews, UI test   gate: feature/notedetail
- [x] notedetail-8 NotesList: "Note deleted" snackbar + Undo (VM + UI tests)   gate: feature/noteslist
- [x] notedetail-9 Navigation wiring + DeepLinkParser + tests, MainActivity intent/onNewIntent   gate: app
- [x] notedetail-10 Strings (en, tr)                                   gate: core/i18n
- [x] notedetail-done Rule of two, full gate + dead code + auto-commit
### noteeditor
- [x] noteeditor-4 Repository: createNote, updateNote (injected Clock) + tests   gate: core/data
- [x] noteeditor-5 :core:notifications rendered + wired (channels, service, ALLOWED_DEEP_LINK_SCHEMES, tr)   gate: core/notifications
- [x] noteeditor-6 ViewModel (assisted noteId?, SavedStateHandle) + validation + tests
        (note_created / note_edited)                                   gate: feature/noteeditor
- [x] noteeditor-7 UI: Route, Screen, text fields, discard dialog, BackHandler, previews, UI test   gate: feature/noteeditor
- [x] noteeditor-8 NotesList FAB + NotificationRationaleCard (rationale → request UI test); NoteDetail edit action   gate: feature/noteslist feature/notedetail
- [x] noteeditor-9 Navigation wiring (from list FAB and detail edit)   gate: app
- [x] noteeditor-10 Strings (en, tr)                                   gate: core/i18n
- [x] noteeditor-done Rule of two, full gate + dead code + auto-commit
### app gate (remote config)
- [ ] config-1 :core:config rendered, strings tr, AppGateViewModel + tests, NotesV2App shows AppGateScreen   gate: core/config app
- [ ] config-done Full gate + dead code + auto-commit

## Stage E — hardening & handover
- [ ] E1 Hardening (pins for jsonplaceholder.typicode.com from the live chain + backup roots)
- [ ] E2 assembleProdRelease (R8)
- [ ] E2b Release smoke test on an emulator (list, create, edit, delete/undo, detail, offline, tr, deep link)
- [ ] E3 Final gate (GATE_FINAL=1)
- [ ] E4 /code-review (high), fix findings with regression tests
- [ ] E5 mobile-codebase-audit → docs/audit/ (≥ 80, every category ≥ 8)
- [ ] E6 Audit fix loop (max 2)
- [ ] E7 Handover checklist → docs/HANDOVER.xlsx
- [ ] E8 README.md, CLAUDE.md, progress.md
- [ ] E9 Final report → ask before final commit / push
