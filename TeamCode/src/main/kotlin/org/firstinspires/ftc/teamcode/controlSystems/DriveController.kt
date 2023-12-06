package org.firstinspires.ftc.teamcode.controlSystems

import org.firstinspires.ftc.teamcode.controlSystems.motionPath.MotionPath
import org.firstinspires.ftc.teamcode.ftcGlue.WithTelemetry
import org.firstinspires.ftc.teamcode.hardware.motion.IDrive
import org.firstinspires.ftc.teamcode.hardware.motion.IOdometry
import org.firstinspires.ftc.teamcode.util.NotForCompetition
import org.firstinspires.ftc.teamcode.util.Vec2Rot
import kotlin.math.abs
import kotlin.math.max

const val MAX_POW = 0.70710678118 // sqrt(1/2)

class DriveController(
        private val output: IDrive,
        private val input: IOdometry,
        control_pid_init: PID1D.Coefficients,
) {
    @NotForCompetition
    fun debugTakeControl(): Pair<IOdometry, IDrive> {
        disableTicking = true
        return Pair(input, output)
    }

    private var disableTicking = false

    var path: MotionPath<Vec2Rot>? = null
    var targetPos: Vec2Rot = input.pos

    fun assertPosition(pos: Vec2Rot) {
        input.assertPosition(pos)
    }

    fun writeTelemetry(t: WithTelemetry.Scope) {
        t.ln("::: DRIVE CONTROLLER :::")
        t.ln("pos.x: ${input.pos.v.x}")
        t.ln("pos.y: ${input.pos.v.y}")
        t.ln("pos.r: ${input.pos.r}")
        t.ln("vel.x: ${input.vel.v.x}")
        t.ln("vel.y: ${input.vel.v.y}")
        t.ln("vel.r: ${input.vel.r}")
        t.ln("::::::::::::::::::::::::")
    }

    private val pid = Control(control_pid_init)

    var t = 0.0
    fun tick(dt: Double) {
        if (disableTicking) return // disables ticking when [debugTakeControlOfOutput] is called.

        t += dt
        input.tick(dt)

        val target = path?.sampleClamped(t)
                ?: MotionPath.PathPoint(targetPos, Vec2Rot.zero, Vec2Rot.zero)
        targetPos = target.pos // save so robot stays in place when [path] is deleted

        val pow = pid.tick(
                input.pos, input.vel,
                target.pos, target.vel,
                dt
        )
        output.power = input.robot2worldTransform().transformVelInv(
                pow * (MAX_POW / max(MAX_POW, max(pow.v.mag, abs(pow.r))))
        )
    }

    fun shutdownOutput() {
        output.power = Vec2Rot.zero
    }

    class Control(init_coefficients: PID1D.Coefficients) {
        private val controlX = QLearningAutoPIDAgent(init_coefficients)
        private val controlY = QLearningAutoPIDAgent(init_coefficients)
        private val controlR = QLearningAutoPIDAgent(init_coefficients)
        fun tick(
                xp: Vec2Rot, xd: Vec2Rot,
                tp: Vec2Rot, td: Vec2Rot,
                dt: Double,
        ): Vec2Rot {
            return Vec2Rot(
                    controlX.tick(xp.v.x, xd.v.x, tp.v.x, td.v.x, dt),
                    controlY.tick(xp.v.y, xd.v.y, tp.v.y, td.v.y, dt),
                    controlR.tick(xp.r, xd.r, tp.r, td.r, dt),
            )
        }
    }
}