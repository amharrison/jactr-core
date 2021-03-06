package org.jactr.core.module.procedural.five.learning;

import org.jactr.core.production.request.IRequest;

/*
 * default logging
 */

public interface ICompilableContext
{
  /**
   * return true if the request on the buffer is immediate, in that they will
   * complete by the time the production has finished firing (not long duration
   * requests like the visual buffer)
   * 
   * @return
   */
  public boolean isImmediate(IRequest request);

  /**
   * returns true if this request to the buffer is deterministic. That is, the
   * requested chunktype (or chunk) is what appears in the buffer - no matter
   * what (no errors and no transformations).
   * 
   * @return
   */
  public boolean isDeterministic(IRequest request);
  
  /**
   * subsequent requests may result in a buffer jam (and abort/error)
   * @return
   */
  public boolean isJammable(IRequest request);
  
  /**
   * if the buffer requests/actions can be compiled out entirely (i.e.
   * the retrieval buffer)
   * @return
   */
  public boolean canCompileOut(IRequest request);

  /**
   * does this request result in a command only and no chunk in the buffer, such
   * as motor or vocal
   *
   * @param request
   * @return
   */
  public boolean isCommandOnly(IRequest request);

  /**
   * The resulting chunk in the buffer can change due to internal/external
   * events other than production manipulation. You want to avoid compiling
   * try-harvests for these buffers. The perceptual style buffers (visual,
   * visual-location, aural, aural-location) fall under this category.
   * 
   * @param request
   * @return
   */
  public boolean chunksCanChange(IRequest request);
}
