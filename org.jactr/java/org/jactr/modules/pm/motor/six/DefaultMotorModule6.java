package org.jactr.modules.pm.motor.six;

/*
 * default logging
 */
import java.util.Collection;
import java.util.Collections;

import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.modules.pm.motor.AbstractMotorModule;
import org.jactr.modules.pm.motor.IMotorModule;
import org.jactr.modules.pm.motor.buffer.IMotorActivationBuffer;
import org.jactr.modules.pm.motor.buffer.six.DefaultMotorActivationBuffer6;
import org.jactr.modules.pm.motor.command.DefaultCommandTranslator;
import org.jactr.modules.pm.motor.command.IMotorTimeEquation;
import org.jactr.modules.pm.motor.command.translators.ClickMouseTranslator;
import org.jactr.modules.pm.motor.command.translators.HandToHome;
import org.jactr.modules.pm.motor.command.translators.HandToMouse;
import org.jactr.modules.pm.motor.command.translators.MoveCursorTranslator;
import org.jactr.modules.pm.motor.command.translators.PeckRecoilTranslator;
import org.jactr.modules.pm.motor.command.translators.PeckTranslator;
import org.jactr.modules.pm.motor.command.translators.PointHandAtKey;
import org.jactr.modules.pm.motor.command.translators.PressKeyTranslator;
import org.jactr.modules.pm.motor.command.translators.PressTranslator;
import org.jactr.modules.pm.motor.command.translators.PunchTranslator;
import org.jactr.modules.pm.motor.command.translators.ReleaseTranslator;
import org.slf4j.LoggerFactory;

public class DefaultMotorModule6 extends AbstractMotorModule
{
  /**
   * Logger definition
   */
  public static final transient org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(DefaultMotorModule6.class);

  private IMotorActivationBuffer                 _buffer;

  public DefaultMotorModule6()
  {
    this("motor");
  }

  public DefaultMotorModule6(String name)
  {
    super(name);
  }

  /**
   * create the {@link IMotorActivationBuffer}
   * 
   * @see org.jactr.core.module.AbstractModule#createBuffers()
   */
  @Override
  protected Collection<IActivationBuffer> createBuffers()
  {
    _buffer = new DefaultMotorActivationBuffer6(IActivationBuffer.MOTOR, this);
    return Collections.singleton((IActivationBuffer) _buffer);
  }

  /**
   * @see IMotorModule#getBuffer()
   */
  public IMotorActivationBuffer getBuffer()
  {
    return _buffer;
  }

  /**
   * initialize by installing the {@link IEfferentObjectTranslator},
   * {@link IEfferentCommandTranslator}, the preparation and processing
   * {@link IMotorTimeEquation}s
   * 
   * @see org.jactr.modules.pm.motor.AbstractMotorModule#initialize()
   */
  @Override
  public void initialize()
  {
    super.initialize();

    setPreparationTimeEquation(new DefaultPreparationTimeEquation());
    setProcessingTimeEquation(new DefaultProcessingTimeEquation());

    DefaultCommandTranslator translator = (DefaultCommandTranslator) getCommandTranslator();

    translator.add(new PeckTranslator());
    translator.add(new PeckRecoilTranslator());
    translator.add(new PunchTranslator());
    translator.add(new PressKeyTranslator());
    translator.add(new HandToHome());
    translator.add(new HandToMouse());
    translator.add(new PointHandAtKey());
    translator.add(new MoveCursorTranslator());
    translator.add(new ClickMouseTranslator());
    translator.add(new PressTranslator());
    translator.add(new ReleaseTranslator());
  }

}
