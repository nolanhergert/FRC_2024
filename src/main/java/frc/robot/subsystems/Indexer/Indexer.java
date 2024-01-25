package frc.robot.subsystems.Indexer;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class Indexer extends SubsystemBase {
  private final IndexerIO io;

  public Indexer(IndexerIO io) {
    this.io = io;
  }

  public void IndexerIn() {
    io.SetVoltage(Constants.IndexerConstants.indexerMotorVoltage);
  }

  public void IndexerOut() {
    io.SetVoltage(-Constants.IndexerConstants.indexerMotorVoltage);
  }

  public void IndexerStop() {
    io.SetVoltage(0);
  }
}
