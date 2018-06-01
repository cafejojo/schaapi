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

internal fun mockStmt() = mock<Stmt> {}

internal fun mockThrowStmt(op: Value) = mock<ThrowStmt> { on { it.op } doReturn op }

internal fun mockDefinitionStmt(leftOp: Value, rightOp: Value) =
    mock<DefinitionStmt> {
        on { it.leftOp } doReturn leftOp
        on { it.rightOp } doReturn rightOp
    }

internal fun mockIfStmt(condition: Value) = mock<IfStmt> { on { it.condition } doReturn condition }

internal fun mockSwitchStmt(key: Value) = mock<SwitchStmt> { on { it.key } doReturn key }

internal fun mockInvokeStmt(invokeExpr: InvokeExpr) = mock<InvokeStmt> { on { it.invokeExpr } doReturn invokeExpr }

internal fun mockReturnStmt(op: Value) = mock<ReturnStmt> { on { it.op } doReturn op }

internal fun mockGotoStmt() = mock<GotoStmt> {}

internal fun mockReturnVoidStmt() = mock<ReturnVoidStmt> {}

internal fun mockValueBox(value: Value) = mock<ValueBox> { on { it.value } doReturn value }

internal fun mockSootMethodRef(sootMethod: SootMethod) = mock<SootMethodRef> { on { it.resolve() } doReturn sootMethod }
