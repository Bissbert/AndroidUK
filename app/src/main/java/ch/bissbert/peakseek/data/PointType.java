package ch.bissbert.peakseek.data;

import com.orm.SugarRecord;

/**
 * Type of Point with a name
 *
 * @author Bissbert
 */
public class PointType extends SugarRecord {
    private String name;

    public PointType() {
    }

    public PointType(String name) {
        this.name = name;
    }

    public Long getId() {
        return super.getId();
    }

    public void setId(Long id) {
        super.setId(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
