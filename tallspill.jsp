<%-- Fra en oppgave - ikke skrevet av meg, kun modifisert (kan komme tilbake til hvem som har skrevet den, fra et universitet) --%>
<%@page contentType="text/plain;charset=UTF-8" pageEncoding="UTF-8"%>

<%
    out.flush();    
    final String FERDIG = "Beklager ingen flere sjanser, du må starte på nytt (registrer kortnummer og navn)";
    
    String navn = request.getParameter("navn");
    
    // Første kall: registrer navn og kortnummer
    if (navn != null) {
        String kortnummer = request.getParameter("kortnummer");
        if (kortnummer == null || kortnummer.isEmpty()) {
            out.println("Feil: kortnummer er ikke oppgitt!");
            return;
        }

        session.setAttribute("navn", navn);
        session.setAttribute("kortnummer", kortnummer);
        session.setAttribute("teller", Integer.valueOf(0));
        
        int riktigTall = new java.util.Random().nextInt(1000) + 1;
        session.setAttribute("riktigTall", Integer.valueOf(riktigTall));
        
        out.println("Oppgi et tall mellom 1 og 1000!");
        return;
    }

    // Hent riktig tall fra session
    Integer rt = (Integer) session.getAttribute("riktigTall");
    if (rt == null) {
        out.println("Du har glemt å støtte cookies, eller du har ikke oppgitt navn og kortnummer i første forespørsel!");
        return;
    }
    int riktigTall = rt.intValue();

    // Hent teller
    Integer ganger = (Integer) session.getAttribute("teller");
    if (ganger == null) {
        out.println("Feil: du må registrere navn og kortnummer før du kan tippe!");
        return;
    }
    int teller = ganger.intValue();

    // Maks antall forsøk
    if (teller >= 50) {
        out.println(FERDIG);
        return;
    }

    // Håndter nytt tall
    String tallStr = request.getParameter("tall");
    if (tallStr == null) {
        out.println("Oppgi et tall mellom 1 og 1000!");
        return;
    }

    int verdi;
    try {
        verdi = Integer.parseInt(tallStr);
    } catch (NumberFormatException e) {
        out.println("Tallet er ikke på riktig form. Det må være et heltall mellom 1 og 1000!");
        return;
    }

    teller++;
    session.setAttribute("teller", Integer.valueOf(teller));

    String melding;
    if (verdi == riktigTall) {
        String k = (String) session.getAttribute("kortnummer");
        String n = (String) session.getAttribute("navn");
        melding = n + ", du har vunnet 100 kr som kommer inn på ditt kort " + k;
        // session.invalidate(); // evt. avslutt session her
    } else if (verdi < riktigTall) {
        melding = "Tallet " + tallStr + " er for lite!";
        if (teller == 50) melding += " " + FERDIG;
    } else {
        melding = "Tallet " + tallStr + " er for stort!";
        if (teller == 50) melding += " " + FERDIG;
    }

    out.println(melding);
%>
