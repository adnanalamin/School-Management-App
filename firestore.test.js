const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated caller cannot read or write school leaves", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("schools/sch_oxford_01/leaves").get());
  await assertFails(
    unauthDb.collection("schools/sch_oxford_01/leaves").doc("lv_1").set({
      id: "lv_1",
      schoolId: "sch_oxford_01",
      status: "PENDING",
    })
  );
});

test("Authenticated user can read and write schools/{schoolId}/leaves", async () => {
  const authDb = testEnv.authenticatedContext("usr_student_1").firestore();
  await assertSucceeds(
    authDb.collection("schools/sch_oxford_01/leaves").doc("lv_1").set({
      id: "lv_1",
      schoolId: "sch_oxford_01",
      requestCode: "LV-OXF-101",
      status: "PENDING",
    })
  );
  await assertSucceeds(authDb.collection("schools/sch_oxford_01/leaves").get());
});

test("Authenticated user can read and write schools/{schoolId}/messages", async () => {
  const authDb = testEnv.authenticatedContext("usr_teacher_1").firestore();
  await assertSucceeds(
    authDb.collection("schools/sch_oxford_01/messages").doc("msg_1").set({
      id: "msg_1",
      schoolId: "sch_oxford_01",
      threadCode: "MSG-OXF-301",
      subject: "Lab Schedule",
    })
  );
  await assertSucceeds(authDb.collection("schools/sch_oxford_01/messages").get());

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("schools/sch_oxford_01/messages").get());
});

test("Authenticated user can read and write schools/{schoolId}/reports", async () => {
  const authDb = testEnv.authenticatedContext("usr_admin_1").firestore();
  await assertSucceeds(
    authDb.collection("schools/sch_oxford_01/reports").doc("rpt_1").set({
      id: "rpt_1",
      schoolId: "sch_oxford_01",
      reportCode: "RPT-OXF-201",
      title: "Executive Digest",
    })
  );
  await assertSucceeds(authDb.collection("schools/sch_oxford_01/reports").get());

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("schools/sch_oxford_01/reports").get());
});

test("Authenticated user cannot access unknown subcollections", async () => {
  const authDb = testEnv.authenticatedContext("usr_student_1").firestore();
  await assertFails(authDb.collection("schools/sch_oxford_01/unknown_sub").get());
});
