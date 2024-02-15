package frc.robot.subsystems.shooter;

import static com.revrobotics.CANSparkBase.ControlType.kVelocity;
import static com.revrobotics.CANSparkBase.IdleMode.kCoast;

import com.revrobotics.CANSparkLowLevel;
import com.revrobotics.CANSparkMax;
import com.revrobotics.SparkPIDController;
import com.revrobotics.SparkPIDController.ArbFFUnits;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;

public class ShooterIOReal implements ShooterIO {
  private boolean openLoop = false;
  // Recalc estimate, TODO characterize
  private final SimpleMotorFeedforward shooterFFModel = new SimpleMotorFeedforward(0.1, 0.26, 0.18);
  private final CANSparkMax topMotorLeader = new CANSparkMax(20, CANSparkMax.MotorType.kBrushless);
  private final CANSparkMax topMotorFollower =
      new CANSparkMax(17, CANSparkMax.MotorType.kBrushless);
  private final CANSparkMax bottomMotorLeader =
      new CANSparkMax(22, CANSparkMax.MotorType.kBrushless);
  private final CANSparkMax bottomMotorFollower =
      new CANSparkMax(21, CANSparkMax.MotorType.kBrushless);

  private final SparkPIDController topMotorPID;
  private final SparkPIDController bottomMotorPID;

  public ShooterIOReal() {
    topMotorLeader.restoreFactoryDefaults();
    topMotorFollower.restoreFactoryDefaults();
    bottomMotorLeader.restoreFactoryDefaults();
    bottomMotorFollower.restoreFactoryDefaults();

    // Tune acceptable current limit, don't want to use all power if shoot while moving
    topMotorLeader.setSmartCurrentLimit(80);
    topMotorFollower.setSmartCurrentLimit(80);
    bottomMotorLeader.setSmartCurrentLimit(80);
    bottomMotorFollower.setSmartCurrentLimit(80);

    topMotorLeader.getEncoder().setVelocityConversionFactor(2);
    topMotorFollower.getEncoder().setVelocityConversionFactor(2);
    bottomMotorLeader.getEncoder().setVelocityConversionFactor(2);
    bottomMotorFollower.getEncoder().setVelocityConversionFactor(2);

    topMotorLeader.setIdleMode(kCoast);
    topMotorFollower.setIdleMode(kCoast);
    bottomMotorLeader.setIdleMode(kCoast);
    bottomMotorFollower.setIdleMode(kCoast);

    topMotorLeader.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus0, 5);
    topMotorLeader.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus1, 5);
    topMotorLeader.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus2, 5);
    topMotorFollower.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus0, 5);
    topMotorFollower.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus1, 5);
    topMotorFollower.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus2, 5);
    bottomMotorLeader.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus0, 5);
    bottomMotorLeader.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus1, 5);
    bottomMotorLeader.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus2, 5);
    bottomMotorLeader.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus0, 5);
    bottomMotorLeader.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus1, 5);
    bottomMotorLeader.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus2, 5);

    topMotorPID = topMotorLeader.getPIDController();
    bottomMotorPID = bottomMotorLeader.getPIDController();

    topMotorPID.setP(1); // TODO tune
    bottomMotorPID.setP(1);

    topMotorFollower.follow(topMotorLeader, true);
    bottomMotorFollower.follow(bottomMotorLeader, true);
  }

  /** Updates the set of loggable inputs. */
  public void updateInputs(ShooterIOInputs inputs) {
    inputs.shooterAppliedVolts =
        new double[] {
          topMotorLeader.getAppliedOutput() * topMotorLeader.getBusVoltage(),
          bottomMotorLeader.getAppliedOutput() * bottomMotorLeader.getBusVoltage()
        };
    inputs.shooterCurrentAmps =
        new double[] {
          topMotorLeader.getOutputCurrent(),
          topMotorFollower.getOutputCurrent(),
          bottomMotorLeader.getOutputCurrent(),
          bottomMotorFollower.getOutputCurrent()
        };
    inputs.shooterVelocityRPM =
        new double[] {
          topMotorLeader.getEncoder().getVelocity(), bottomMotorLeader.getEncoder().getVelocity()
        };
    inputs.openLoopStatus = openLoop;
  }

  /** Run open loop at the specified voltage. Primarily for characterization. */
  public void setVoltage(double volts) {
    openLoop = true;
    topMotorLeader.setVoltage(volts);
    bottomMotorLeader.setVoltage(volts);
  }

  /** Run closed loop at the specified velocity. */
  public void setVelocity(double velocityRPM) {
    openLoop = false;
    topMotorPID.setReference(
        velocityRPM, kVelocity, 0, shooterFFModel.calculate(velocityRPM), ArbFFUnits.kVoltage);
    bottomMotorPID.setReference(
        velocityRPM, kVelocity, 0, shooterFFModel.calculate(velocityRPM), ArbFFUnits.kVoltage);
  }

  /** Stop in open loop. */
  public void stop() {
    openLoop = true;
    setVoltage(0.0);
  }
}
