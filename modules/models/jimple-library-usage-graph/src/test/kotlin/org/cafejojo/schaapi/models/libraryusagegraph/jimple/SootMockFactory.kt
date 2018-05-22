package org.cafejojo.schaapi.models.libraryusagegraph.jimple

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import soot.SootMethod
import soot.SootMethodRef
import soot.Value
import soot.ValueBox
import soot.jimple.DefinitionStmt
import soot.jimple.GotoStmt
import soot.jimple.IfStmt
import soot.jimple.InvokeExpr
import soot.jimple.InvokeStmt
import soot.jimple.ReturnStmt
import soot.jimple.ReturnVoidStmt
import soot.jimple.Stmt
import soot.jimple.SwitchStmt
import soot.jimple.ThrowStmt

fun mockStmt() = mock<Stmt> {}

fun mockThrowStmt(op: Value) = mock<ThrowStmt> { on { it.op } doReturn op }

fun mockDefinitionStmt(leftOp: Value, rightOp: Value) =
    mock<DefinitionStmt> {
        on { it.leftOp } doReturn leftOp
        on { it.rightOp } doReturn rightOp
    }

fun mockIfStmt(condition: Value) = mock<IfStmt> { on { it.condition } doReturn condition }

fun mockSwitchStmt(key: Value) = mock<SwitchStmt> { on { it.key } doReturn key }

fun mockInvokeStmt(invokeExpr: InvokeExpr) = mock<InvokeStmt> { on { it.invokeExpr } doReturn invokeExpr }

fun mockReturnStmt(op: Value) = mock<ReturnStmt> { on { it.op } doReturn op }

fun mockGotoStmt() = mock<GotoStmt> {}

fun mockReturnVoidStmt() = mock<ReturnVoidStmt> {}

fun mockValueBox(value: Value) = mock<ValueBox> { on { it.value } doReturn value }

fun mockSootMethodRef(sootMethod: SootMethod) = mock<SootMethodRef> { on { it.resolve() } doReturn sootMethod }
