package jflowmap.views.flowmap;

import java.util.List;

import jflowmap.data.SeqStat;
import jflowmap.views.Legend.ItemProducer;

import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PNode;

/**
 * @author Ilya Boyandin
 */
public abstract class AbstractLegendItemProducer implements ItemProducer {

  private final int numLegendValues;

  public AbstractLegendItemProducer(int numLegendValues) {
    this.numLegendValues = numLegendValues;
  }

  @Override
  public PNode createHeader() {
    return null;
  }

  @Override
  public Iterable<PNode> createItems() {
    List<PNode> items = Lists.newArrayList();

    SeqStat stats = getValueStat();
    List<Double> values = LegendValuesGenerator.generate(
        stats.getMin(), stats.getMax(), numLegendValues);
    for (Double value : values) {
      items.add(createItem(value));
    }

    return items;
  }

  public abstract PNode createItem(double value);

  public abstract SeqStat getValueStat();

}
