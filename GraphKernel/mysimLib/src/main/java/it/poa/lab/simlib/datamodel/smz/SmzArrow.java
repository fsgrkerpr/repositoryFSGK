package it.poa.lab.simlib.datamodel.smz;

import it.poa.lab.simlib.datamodel.Arrow;

/**
 * Created by giorgio on 12/05/16.
 */
public class SmzArrow extends Arrow{

    private int occurrence = 0;

    public SmzArrow(SmzNode property, String direction) {
        super(property, direction);
    }

    public int getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(int occurrence) {
        this.occurrence = occurrence;
    }
}
