package org.firstinspires.ftc.teamcode.robot.hbot.opmodes

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.LOpMode
import org.firstinspires.ftc.teamcode.robot.hbot.HBotRobot

@TeleOp
class HBotTestIntakeOp : LOpMode<HBotRobot.Impl>(HBotRobot, {
    robot.intake.pos = 0.0

    waitForStart()

    createLoop {
        robot.intake.tick(dt)

        val x = gamepadA.stick.left.pos.x
        robot.intake.controller.targetPosition = x
    }

})