const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: "127.0.0.1",
      port: 8085,
    },
  });
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

test("unauthenticated access is rejected for all collections", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.collection("customers").get());
  await assertFails(unauthedDb.collection("invoices").get());
  await assertFails(unauthedDb.collection("payments").get());
});

test("authenticated user can create and read their own customer", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const customerRef = aliceDb.collection("customers").doc("cust_1");
  await assertSucceeds(
    customerRef.set({
      userId: ALICE_UID,
      name: "Aditya Infra Projects",
      phone: "+91 94220 11223",
      address: "Kharadi, Pune",
      isActive: true,
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  // Bob cannot read Alice's customer
  await assertFails(bobDb.collection("customers").doc("cust_1").get());
});

test("authenticated user can create invoice and enforce validation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const invoiceRef = aliceDb.collection("invoices").doc("inv_1");
  await assertSucceeds(
    invoiceRef.set({
      userId: ALICE_UID,
      invoiceNumber: "MKLDB-2026-001",
      customerName: "Aditya Infra Projects",
      grandTotal: 18000.0,
      balanceDue: 18000.0,
      status: "UNPAID",
    })
  );

  // Rejects invalid invoice without required fields
  await assertFails(
    aliceDb.collection("invoices").doc("inv_bad").set({
      userId: ALICE_UID,
      // Missing invoiceNumber & grandTotal
      status: "UNPAID",
    })
  );
});
