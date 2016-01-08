/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.entidades;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Mario
 */
@Entity
@Table(name = "cit_proposito_consulta", catalog = "openmedical", schema = "public")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CitPropositoConsulta.findAll", query = "SELECT c FROM CitPropositoConsulta c"),
    @NamedQuery(name = "CitPropositoConsulta.findByIdCita", query = "SELECT c FROM CitPropositoConsulta c WHERE c.citPropositoConsultaPK.idCita = :idCita"),
    @NamedQuery(name = "CitPropositoConsulta.findByIdProposito", query = "SELECT c FROM CitPropositoConsulta c WHERE c.citPropositoConsultaPK.idProposito = :idProposito"),
    @NamedQuery(name = "CitPropositoConsulta.findByValorCampo", query = "SELECT c FROM CitPropositoConsulta c WHERE c.valorCampo = :valorCampo")})
public class CitPropositoConsulta implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CitPropositoConsultaPK citPropositoConsultaPK;
    @Column(name = "valor_campo")
    private Boolean valorCampo;
    @JoinColumn(name = "id_proposito", referencedColumnName = "id_proposito", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private CfgPropositoConsulta cfgPropositoConsulta;
    @JoinColumn(name = "id_cita", referencedColumnName = "id_cita", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private CitCitas citCitas;

    public CitPropositoConsulta() {
    }

    public CitPropositoConsulta(CitPropositoConsultaPK citPropositoConsultaPK) {
        this.citPropositoConsultaPK = citPropositoConsultaPK;
    }

    public CitPropositoConsulta(int idCita, int idProposito) {
        this.citPropositoConsultaPK = new CitPropositoConsultaPK(idCita, idProposito);
    }

    public CitPropositoConsultaPK getCitPropositoConsultaPK() {
        return citPropositoConsultaPK;
    }

    public void setCitPropositoConsultaPK(CitPropositoConsultaPK citPropositoConsultaPK) {
        this.citPropositoConsultaPK = citPropositoConsultaPK;
    }

    public Boolean getValorCampo() {
        return valorCampo;
    }

    public void setValorCampo(Boolean valorCampo) {
        this.valorCampo = valorCampo;
    }

    public CfgPropositoConsulta getCfgPropositoConsulta() {
        return cfgPropositoConsulta;
    }

    public void setCfgPropositoConsulta(CfgPropositoConsulta cfgPropositoConsulta) {
        this.cfgPropositoConsulta = cfgPropositoConsulta;
    }

    public CitCitas getCitCitas() {
        return citCitas;
    }

    public void setCitCitas(CitCitas citCitas) {
        this.citCitas = citCitas;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (citPropositoConsultaPK != null ? citPropositoConsultaPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CitPropositoConsulta)) {
            return false;
        }
        CitPropositoConsulta other = (CitPropositoConsulta) object;
        if ((this.citPropositoConsultaPK == null && other.citPropositoConsultaPK != null) || (this.citPropositoConsultaPK != null && !this.citPropositoConsultaPK.equals(other.citPropositoConsultaPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entidades.CitPropositoConsulta[ citPropositoConsultaPK=" + citPropositoConsultaPK + " ]";
    }
    
}
