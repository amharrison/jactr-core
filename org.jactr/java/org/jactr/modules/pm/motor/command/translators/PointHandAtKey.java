package org.jactr.modules.pm.motor.command.translators;

import java.util.Optional;

import org.commonreality.sensors.keyboard.map.ACTRDeviceMap;
import org.jactr.core.model.IModel;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.slot.ISlot;

public class PointHandAtKey extends AbstractHandToTranslator
{
  ACTRDeviceMap _deviceMap = new ACTRDeviceMap();

  public PointHandAtKey()
  {

  }

  @Override
  public boolean handles(ChunkTypeRequest request)
  {
    return handles("point-hand-at-key", request);
  }

  @Override
  protected void testPosition(IModel model) throws IllegalArgumentException
  {

  }

  @Override
  protected double[] getTarget(ChunkTypeRequest request)
  {
    _recycledSlotContainer.clear();
    Optional<ISlot> keySlot = request.getSlots(_recycledSlotContainer).stream()
        .filter((s) -> s.getName().equals("to-key")).findFirst();

    if (!keySlot.isPresent()) throw new IllegalArgumentException(
        "point-hand-at-key needs slot named to-key to the defined");

    char key = keySlot.get().getValue().toString().charAt(0);
    int keyCode = _deviceMap.getKeyCode("" + Character.toUpperCase(key));
    return _deviceMap.getKeyLocation(keyCode);
  }

}
