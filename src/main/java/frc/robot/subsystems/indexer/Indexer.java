package frc.robot.subsystems.indexer;

import static frc.robot.subsystems.indexer.IndexerIO.IndexerIOInputs.MotorState.*;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.function.BooleanSupplier;
import org.littletonrobotics.junction.Logger;

public class Indexer extends SubsystemBase {
  private final IndexerIO io;
  private final IndexerIOInputsAutoLogged inputs = new IndexerIOInputsAutoLogged();

  public enum indexerStates {
    INDEX_ON_INTAKE_ON,
    INDEX_ON_INTAKE_OFF,
    INDEX_OFF_INTAKE_ON,
    INDEX_OFF_INTAKE_OFF,
  }

  indexerStates state = indexerStates.INDEX_OFF_INTAKE_ON;

  public Indexer(IndexerIO io) {
    this.io = io;
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Indexer", inputs);

    switch (state) {
      case INDEX_ON_INTAKE_ON:
        indexerIn();
        intakeIn();
        if (inputs.shooterBeamBreakState) {
          state = indexerStates.INDEX_OFF_INTAKE_OFF;
        }
        break;
      case INDEX_ON_INTAKE_OFF:
        indexerIn();
        intakeStop();
        if (!inputs.shooterBeamBreakState) {
          state = indexerStates.INDEX_OFF_INTAKE_ON;
        }
        break;
      case INDEX_OFF_INTAKE_ON:
        indexerStop();
        intakeIn();
        if (inputs.intakeBeamBreakState) {
          state = indexerStates.INDEX_ON_INTAKE_ON;
        }
        break;
      case INDEX_OFF_INTAKE_OFF:
        indexerStop();
        intakeStop();
        break;
      default:
        indexerStop();
        intakeStop();
        break;
    }
  }

  public Command getDefaultCommand(BooleanSupplier pivotHandoff) {
    return run(this::indexOffIntakeOn)
        .until(() -> inputs.intakeBeamBreakState)
        .andThen(run(this::indexOffIntakeOff))
        .until(pivotHandoff)
        .andThen(run(this::indexOnIntakeOn))
        .until(() -> inputs.shooterBeamBreakState)
        .finallyDo(this::indexOffIntakeOff);
  }

  public void indexOffIntakeOn() {
    io.setShooterIndexer(OFF);
    io.setIntakeIndexer(IN);
  }

  public void indexOnIntakeOn() {
    io.setShooterIndexer(IN);
    io.setIntakeIndexer(IN);
  }

  public void indexOffIntakeOff() {
    io.setIntakeIndexer(OFF);
    io.setShooterIndexer(OFF);
  }

  public void indexerIn() {
    io.setShooterIndexer(IN);
  }

  public void indexerOut() {
    io.setShooterIndexer(OUT);
  }

  public void indexerStop() {
    io.setShooterIndexer(OFF);
  }

  public void intakeIn() {
    io.setIntakeIndexer(IN);
  }

  public void intakeOut() {
    io.setIntakeIndexer(OUT);
  }

  public void intakeStop() {
    io.setIntakeIndexer(OFF);
  }

  // Commands

  public Command intakeInCommand() {
    return runOnce(this::intakeIn);
  }

  public Command intakeOutCommand() {
    return runOnce(this::intakeOut);
  }

  public Command intakeStopCommand() {
    return runOnce(this::intakeStop);
  }

  public Command indexerInCommand() {
    return runOnce(this::indexerIn);
  }

  public Command indexerOutCommand() {
    return runOnce(this::indexerOut);
  }

  public Command indexerStopCommand() {
    return runOnce(this::indexerStop);
  }
}
