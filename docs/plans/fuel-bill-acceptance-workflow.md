# Fuel Bill Acceptance Workflow — Master Plan

Status legend: `[ ]` not started · `[~]` in progress · `[x]` done

## 1. Problem

Fuel transactions are marked "submitted to payment" and bundled into a `Bill`
(`FuelRequestAndIssueController.makePaymentRequest`). Today a `Bill` has no
concept of CPC acceptance — it's just printed. We need:

1. CPC (regional office / CPC head office **only** — not the fuel station
   that issued the fuel) must **Accept** a bill, or **ask for Resubmit**
   (no hard "Reject").
2. Status visible at both **bill level** and **individual transaction
   level**, on every list/view that shows either.
3. A bill cannot be accepted twice.
4. A transaction cannot end up in more than one bill (already filtered in
   the UI query — add a server-side guard too, defense in depth).
5. Once a bill is **Accepted**, admins cannot edit/delete its transactions.
   To unlock, CPC must first **Cancel the acceptance**.
6. Full audit trail of every status transition.
7. Safe to abandon: additive-only schema changes, all work on a feature
   branch, nothing touches `main`/production until reviewed and merged.

## 2. Design decisions (confirmed with user)

- **Who can decide:** CPC regional office / CPC head office roles only
  (`CPC_ADMINISTRATOR`, `CPC_SUPER_USER`, or any user at a
  `CPC_HEAD_OFFICE`-category institution). The fuel station itself
  (`CPC_USER`, `CPC_FUEL_DISPENSOR` at the issuing station) cannot accept
  its own bill.
- **Resubmit loop:** CPC → `RESUBMIT_REQUESTED` (with required comments) →
  admin edits the flagged transactions → admin clicks an explicit
  **"Resubmit Bill"** button → status back to `PENDING` → CPC decides again.
  No implicit/automatic flips.
- **Status storage:** mirrored directly on `FuelTransaction`, the same way
  `submittedToPayment`/`submittedToPaymentAt` already mirror bill-submission
  state (not a computed join) — set in the same loop that already touches
  every line item on a status change.
- **Double-accept / race safety:** status-guard (no-op transition rejected)
  plus optimistic locking (`@Version`) on `Bill`.
- **Schema:** EclipseLink `create-or-extend-tables` — new nullable
  columns/tables appear automatically on redeploy; no manual migration
  script, and rolling back the code leaves harmless unused columns.

## 3. Data model changes

- New enum `BillAcceptanceStatus`: `PENDING`, `ACCEPTED`, `RESUBMIT_REQUESTED`.
- `Bill`: `acceptanceStatus` (default PENDING), `acceptedBy`/`acceptedAt`,
  `resubmitRequestedBy`/`At`/`resubmitComments`,
  `acceptanceCancelledBy`/`At`/`Comments`, `resubmittedBy`/`At`
  (admin-side "fixed and resubmitted"), `@Version version` for optimistic
  locking. Convenience booleans `isAccepted()`/`isPendingDecision()`/
  `isResubmitRequested()`.
- New entity `BillAcceptanceHistory`: one row per transition (bill, from
  status, to status, changed by/at, comments) — full audit trail,
  independent of the existing `BillHistory` (which only tracks total-qty
  drift on reprint).
- `FuelTransaction`: mirrored `billAcceptanceStatus` + `billAcceptanceStatusAt`
  fields, set/cleared in lockstep with the `Bill` whenever its status
  changes (same pattern as the existing `submittedToPayment*` fields).
  Convenience `isBillLocked()` → `true` when `billAcceptanceStatus ==
  ACCEPTED`.

## 4. Backend logic (`FuelRequestAndIssueController` unless noted)

- `acceptBill()` — guard: status must be `PENDING`; authorization guard;
  sets ACCEPTED fields; mirrors to every line item; writes history.
- `requestResubmit(String comments)` — guard: status `PENDING`; comments
  required; sets `RESUBMIT_REQUESTED` + comments; mirrors; history.
- `cancelAcceptance(String comments)` — guard: status `ACCEPTED`; reopens
  to `PENDING`; mirrors (clears lock); history.
- `resubmitBillByAdmin()` — guard: status `RESUBMIT_REQUESTED`; sets back
  to `PENDING`; mirrors; history. Called by the admin/hospital side after
  fixing the flagged transactions.
- Authorization helper restricted per §2.
- `OptimisticLockException` caught around every `billFacade.edit(...)` in
  the above → friendly "someone already acted on this bill, please
  refresh" message instead of a stack trace.
- `makePaymentRequest()`: add a fresh re-check (re-fetch each selected
  transaction, confirm `!isSubmittedToPayment()`) immediately before
  creating the bill — defense in depth alongside the existing query filter.
- `ReportController.saveSelected()` / `deleteSelected()` /
  `reverseDeletionSelected()` (the single choke point for all admin
  transaction edit/delete UI — `request.xhtml`, `request_1.xhtml`,
  `request_edit.xhtml`, `request_delete.xhtml`): block with an explanatory
  error message when `fuelTransaction.isBillLocked()`.

## 5. UI changes

- Bill-list pages (`reports/cpc/payment_requests.xhtml`,
  `reports/cpc_head_office/payment_requests.xhtml`, and the admin-side
  `requests/list_to_paid*.xhtml` transaction lists): status column/badge
  (Pending / Accepted / Resubmit Requested).
- `requests/list_payment.xhtml` (the printed bill, shared by create/reprint/
  CPC-view): status banner with resubmit comments when present; Accept /
  Request-Resubmit buttons for authorized CPC viewers on `PENDING` bills;
  Cancel-Acceptance button on `ACCEPTED` bills; Resubmit-Bill button for the
  admin/hospital side on `RESUBMIT_REQUESTED` bills.
- `reports/request.xhtml` (+ `request_1.xhtml`, `request_edit.xhtml`,
  `request_delete.xhtml`): "Payment Bill Status" block; Save/Delete/Reverse
  buttons hidden/disabled with an explanatory message when locked.

## 6. Safety / rollback

- All work on branch `feature/bill-acceptance-workflow`; `main` untouched
  until reviewed. Abandoning the branch returns the repo to today's state
  exactly.
- Additive-only schema (see §2) — no destructive DDL at any phase.
- Build (`mvn -q compile`) after every phase before moving on.
- Nothing gets deployed to the live Payara instance without an explicit
  go-ahead, since this checkout **is** production
  ([[fmis-deployment-targets]]).

## 7. Progress

- [x] Phase 0 — Investigate current model, discuss & confirm design, write
      this plan, create feature branch.
- [x] Phase 1 — Entities: `BillAcceptanceStatus` enum, `Bill` fields +
      `@Version`, `BillAcceptanceHistory` entity + facade, `FuelTransaction`
      mirror fields. Compiles clean.
- [x] Phase 2 — Controller logic: accept/requestResubmit/cancelAcceptance/
      resubmitBillByAdmin, authorization helper (regional/head office only,
      never the fuel station itself), optimistic-lock handling,
      makePaymentRequest re-check guard. Compiles clean.
- [x] Phase 3 — Edit-lock enforcement in `ReportController`
      (saveSelected/deleteSelected/reverseDeletionSelected). Compiles clean.
- [x] Phase 4 — UI: bill-list status columns (`cpc/payment_requests.xhtml`,
      `cpc_head_office/payment_requests.xhtml`), transaction-list status
      columns (6 pages that already showed `submittedToPayment`),
      `list_payment.xhtml` banner + Accept/Request-Resubmit/
      Cancel-Acceptance/Resubmit-Bill actions with comment dialogs,
      `request*.xhtml` (4 pages) lock indicator + disabled/hidden
      Save/Delete/Reverse controls. Compiles clean; all touched XHTML
      verified well-formed.
- [x] Phase 5 — Compile, manual smoke test, review with user, PR opened
      for QA. PR: https://github.com/lk-gov-health-hiu/fmis/pull/152
      (branch `feature/bill-acceptance-workflow`). Deployed to a QA
      instance (`fmis` app on `domain1`, backed by the `fmis_test`
      database) and smoke-tested live by the user.
- [x] Phase 6 — Bugs found during live QA, fixed on this branch:
      - `FuelTransactionLight` (the lightweight DTO backing
        `reports/cpc/list.xhtml`, `cpc_head_office/list.xhtml`,
        `reports/list.xhtml`, `list_institution.xhtml`,
        `institution/reports/list.xhtml`) never got a
        `billAcceptanceStatus` field/getters added, even though those 5
        pages already reference `#{transaction.billStatusPending}` etc. —
        crashed every one of those list pages with
        `PropertyNotFoundException`. Added the field, three convenience
        booleans, two new constructor overloads, and updated all three
        JPQL constructor-select queries in `ReportController.java` to
        select `ft.billAcceptanceStatus`.
      - `acceptBill()` / `requestResubmitOfBill()` / `cancelBillAcceptance()`
        / `resubmitBillByAdmin()` only caught `OptimisticLockException`
        around `billFacade.edit(bill)`. Under GlassFish/Payara's
        container-managed transactions, a genuine lock conflict (e.g. a
        double-click, or two CPC users racing on the same bill) can
        instead surface as `javax.ejb.EJBTransactionRolledbackException`
        — a sibling type, not caught by the existing block — producing an
        uncaught HTTP 500 instead of the intended friendly "someone
        already acted on this bill" message. Broadened all four catches
        to `catch (OptimisticLockException | EJBTransactionRolledbackException ole)`.
      - Added a "Back" button next to Print on `list_payment.xhtml`
        (`history.back()`) — there was previously no way back to the
        list except the browser's own back button or re-navigating via
        the menu.
      - See §8 below for the schema-backfill step this branch's QA also
        surfaced as a hard requirement on any database with pre-existing
        `Bill`/`FuelTransaction` rows.

## 8. Deployment checklist (read this before/after merging to `main`)

EclipseLink's `create-or-extend-tables` (see §2/§3) adds the new columns
and tables automatically on first deploy against a given database. That
covers the schema shape, but **does not backfill values into the new
columns on pre-existing rows** — two things need explicit follow-up:

1. **Mandatory: backfill `Bill.version`.** `Bill.acceptanceStatus` is
   read through a self-healing getter (`null` → treated as `PENDING`,
   see `Bill.getAcceptanceStatus()`), so it needs no migration. `version`
   (the new `@Version` optimistic-lock field) has no such getter — it's
   read directly by EclipseLink/JPA. A `NULL` version on a pre-existing
   `Bill` row throws `OptimisticLockException` ("no version number in
   the identity map") the **first time anyone edits that bill** (accept,
   resubmit-request, cancel-acceptance, or resubmit-by-admin — any
   `billFacade.edit(bill)` call). Run this once, right after deploying to
   any database that already has `Bill` rows (i.e. every real environment
   except a brand-new empty DB):
   ```sql
   UPDATE BILL SET VERSION = 1 WHERE VERSION IS NULL;
   ```
   Confirm afterward with `SELECT COUNT(*) FROM BILL WHERE VERSION IS NULL;`
   (should be 0).

2. **Verify the schema actually landed, don't just trust the deploy log.**
   On a database with a lot of prior schema drift (an old/rarely-migrated
   environment), `create-or-extend-tables` can silently skip adding some
   columns to an existing table and only fail loudly later, when it tries
   to add a foreign-key constraint that references a column it never
   added (`SQLSyntaxErrorException: Key column '...' doesn't exist in
   table`) — by which point other, unrelated missing columns may already
   have been skipped without any error at all. This bit us hard in QA: a
   database that hadn't been touched since 2023 was missing 14
   `FUELTRANSACTION` columns spanning multiple *unrelated* older features
   (`submittedToPayment`, `paymentBill`, `createdBy`/`createdAt`, etc.),
   not just this PR's additions. After the first deploy to any database
   you haven't specifically verified is current, diff every touched
   entity's fields against the real table:
   ```sql
   DESCRIBE BILL; DESCRIBE FUELTRANSACTION;
   DESCRIBE BILLHISTORY; DESCRIBE BILLACCEPTANCEHISTORY;
   ```
   against `Bill.java` / `FuelTransaction.java` (unannotated fields map to
   `FIELDNAME.toUpperCase()` for scalars, `FIELDNAME_ID` for `@ManyToOne`
   — an explicit `@Column`/`@JoinColumn` overrides that). `BILLHISTORY`
   and `BILLACCEPTANCEHISTORY` are brand-new tables so they're created
   fresh in one shot and don't need this check; `BILL` and
   `FUELTRANSACTION` are existing tables and do.

Neither step is needed on a genuinely empty/fresh database (nothing to
backfill, nothing to drift). Both are one-time, per-database — not
needed again on subsequent deploys once done.

Two more things worth knowing about, but specific to how this branch was
QA'd rather than something this PR's code needs to fix:
- If QA-ing behind an nginx reverse proxy, check it for
  `proxy_redirect http:// https://;` on whatever `location` block fronts
  this app — that directive rewrites *any* backend redirect to `https://`
  unconditionally, which breaks every `faces-redirect=true` JSF
  navigation (35+ in `ReportController.java` alone) if the app isn't
  actually served over TLS on that listener/port.
- If pointing a freshly-created JDBC connection pool at this app (rather
  than reusing an already-tuned production one), give it connection
  validation (`is-connection-validation-required=true`,
  `connection-validation-method=auto-commit`) — otherwise a MySQL
  connection silently dropped for being idle surfaces as a raw
  `EOFException` instead of being transparently replaced.
