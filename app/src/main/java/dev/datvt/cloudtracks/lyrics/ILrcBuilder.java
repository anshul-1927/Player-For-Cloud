/**
 * douzifly @Aug 10, 2013
 * github.com/douzifly
 * douzifly@gmail.com
 */
package dev.datvt.cloudtracks.lyrics;

import java.util.List;

/**
 * @author douzifly
 *
 */
public interface ILrcBuilder {
    List<LrcRow> getLrcRows(String rawLrc);
}
