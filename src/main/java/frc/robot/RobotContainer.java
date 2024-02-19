// Copyright 2021-2024 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

package frc.robot;

import static frc.robot.Constants.FieldConstants.getSpeaker;
import static frc.robot.Constants.ShooterConstants.shooterSpeedRPM;
import static frc.robot.commands.SpeakerCommands.shootSpeaker;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.path.PathPlannerPath;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.commands.DriveCommands;
import frc.robot.subsystems.apriltagvision.AprilTagVisionIO;
import frc.robot.subsystems.apriltagvision.AprilTagVisionIOReal;
import frc.robot.subsystems.apriltagvision.AprilTagVisionIOSim;
import frc.robot.subsystems.climber.Climber;
import frc.robot.subsystems.climber.ClimberIO;
import frc.robot.subsystems.climber.ClimberIOSim;
import frc.robot.subsystems.climber.ClimberIOSparkMax;
import frc.robot.subsystems.drive.*;
import frc.robot.subsystems.indexer.Indexer;
import frc.robot.subsystems.indexer.IndexerIO;
import frc.robot.subsystems.indexer.IndexerIOSim;
import frc.robot.subsystems.indexer.IndexerIOSparkMax;
import frc.robot.subsystems.pivot.Pivot;
import frc.robot.subsystems.pivot.PivotIO;
import frc.robot.subsystems.pivot.PivotIOReal;
import frc.robot.subsystems.pivot.PivotIOSim;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.ShooterIO;
import frc.robot.subsystems.shooter.ShooterIOReal;
import frc.robot.subsystems.shooter.ShooterIOSim;
import org.littletonrobotics.junction.AutoLogOutput;
import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // Subsystems
  private final Drive drive;
  private final Indexer indexer;
  private final Pivot pivot;
  private final Shooter shooter;
  private final Climber climber;

  // Controller
  private final CommandXboxController controller = new CommandXboxController(0);

  // Dashboard inputs
  private final LoggedDashboardChooser<Command> autoChooser;

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    switch (Constants.currentMode) {
      case REAL:
        // Real robot, instantiate hardware IO implementations
        drive =
            new Drive(
                new GyroIONavX(),
                new ModuleIOTalonFX(0),
                new ModuleIOTalonFX(1),
                new ModuleIOTalonFX(2),
                new ModuleIOTalonFX(3),
                new AprilTagVisionIOReal());
        indexer = new Indexer(new IndexerIOSparkMax());
        pivot = new Pivot(new PivotIOReal());
        shooter = new Shooter(new ShooterIOReal());
        climber = new Climber(new ClimberIOSparkMax());
        break;

      case SIM:
        // Sim robot, instantiate physics sim IO implementations
        drive =
            new Drive(
                new GyroIO() {},
                new ModuleIOSim(),
                new ModuleIOSim(),
                new ModuleIOSim(),
                new ModuleIOSim(),
                new AprilTagVisionIOSim());
        indexer = new Indexer(new IndexerIOSim());
        pivot = new Pivot(new PivotIOSim());
        shooter = new Shooter(new ShooterIOSim());
        climber = new Climber(new ClimberIOSim());
        break;

      default:
        // Replayed robot, disable IO implementations
        drive =
            new Drive(
                new GyroIO() {},
                new ModuleIO() {},
                new ModuleIO() {},
                new ModuleIO() {},
                new ModuleIO() {},
                new AprilTagVisionIO() {});
        indexer = new Indexer(new IndexerIO() {});
        pivot = new Pivot(new PivotIO() {});
        shooter = new Shooter(new ShooterIO() {});
        climber = new Climber(new ClimberIO() {});
        break;
    }

    // Set up auto routines
    NamedCommands.registerCommand(
        "Nine Piece Auto",
        Commands.runOnce(
                () ->
                    drive.setPose(
                        PathPlannerPath.fromChoreoTrajectory("ninepieceauto")
                            .getTrajectory(new ChassisSpeeds(), new Rotation2d())
                            .getInitialTargetHolonomicPose()))
            .andThen(
                AutoBuilder.followPath(PathPlannerPath.fromChoreoTrajectory("ninepieceauto"))));
    autoChooser = new LoggedDashboardChooser<>("Auto Choices", AutoBuilder.buildAutoChooser());

    autoChooser.addOption("Nine Piece Auto", NamedCommands.getCommand("Nine Piece Auto"));
    // Set up SysId routines
    autoChooser.addOption("Swerve Drive SysId Routine", drive.runDriveCharacterizationCmd());
    autoChooser.addOption("Swerve Steer SysId Routine", drive.runModuleSteerCharacterizationCmd());

    // Configure the button bindings
    configureButtonBindings();
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    drive.setDefaultCommand(
        DriveCommands.joystickDrive(
            drive,
            () -> -controller.getLeftY(),
            () -> -controller.getLeftX(),
            () -> -controller.getRightX(),
            () -> controller.getLeftTriggerAxis() > 0.5)); // Trigger locks make trigger 0/1
    indexer.setDefaultCommand(indexer.getDefault(pivot::inHandoffZone));
    pivot.setDefaultCommand(pivot.getDefault());
    shooter.setDefaultCommand(shooter.getDefault());

    // Subwoofer
    controller
        .b()
        .onTrue(pivot.setPositionCommand(() -> Units.degreesToRadians(60)).until(pivot::atGoal));
    // Trap
    controller
        .y()
        .onTrue(pivot.setPositionCommand(() -> Units.degreesToRadians(100)).until(pivot::atGoal));
    // Amp
    controller
        .x()
        .onTrue(pivot.setPositionCommand(() -> Units.degreesToRadians(100)).until(pivot::atGoal));

    // Climb Manual Up
    controller.povLeft().whileTrue(climber.runVoltsCommand(6.0));
    // Climb Manual Down
    controller.povRight().whileTrue(climber.runVoltsCommand(-6.0));

    // Intake Manual In
    controller.rightBumper().whileTrue(indexer.intakeInCommand());
    // "Intake Out" - Indexer Manual Run
    controller.leftBumper().whileTrue(indexer.indexerInCommand());

    // Auto Rotation Lock Shooter Pivot Interp
    controller
        .a()
        .whileTrue(
            shootSpeaker(drive, pivot, () -> -controller.getLeftY(), () -> -controller.getLeftX()));

    // TODO what button IDs are these
    // Z, climb up
    controller.button(20).whileTrue(climber.setPositionCommand(0.75));
    // C, climb down
    controller.button(21).whileTrue(climber.setPositionCommand(0.05));

    controller
        .rightTrigger()
        .whileTrue(
            shooter
                .setVelocityRPMCommand(shooterSpeedRPM)
                .alongWith(
                    Commands.waitUntil(shooter::isAtShootSpeed).andThen(indexer.shootCommand())));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return autoChooser.get();
  }

  @AutoLogOutput
  public Pose3d[] log3DPoses() {
    Pose3d[] mechanismPoses = new Pose3d[1];
    mechanismPoses[0] = pivot.getPose3D();
    return mechanismPoses;
  }
}
