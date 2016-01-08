/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.entidades;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author Mario
 */
@Embeddable
public class CitPropositoConsultaPK implements Serializable {
    @Basic(optional = false)
    @Column(name = "id_cita", nullable = false)
    private int idCita;
    @Basic(optional = false)
    @Column(name = "id_proposito", nullable = false)
    private int idProposito;

    public CitPropositoConsultaPK() {
    }

    public CitPropositoConsultaPK(int idCita, int idProposito) {
        this.idCita = idCita;
        this.idProposito = idProposito;
    }

    public int getIdCita() {
        return idCita;
    }

    public void setIdCita(int idCita) {
        this.idCita = idCita;
    }

    public int getIdProposito() {
        return idProposito;
    }

    public void setIdProposito(int idProposito) {
        this.idProposito = idProposito;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) idCita;
        hash += (int) idProposito;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CitPropositoConsultaPK)) {
            return false;
        }
        CitPropositoConsultaPK other = (CitPropositoConsultaPK) object;
        if (this.idCita != other.idCita) {
            return false;
        }
        if (this.idProposito != other.idProposito) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entidades.CitPropositoConsultaPK[ idCita=" + idCita + ", idProposito=" + idProposito + " ]";
    }
    
}
