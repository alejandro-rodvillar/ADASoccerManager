const functions = require("firebase-functions"); // ⬅️ V1 IMPORTACIÓN
const admin = require("firebase-admin");

admin.initializeApp();

const db = admin.database();

function getRandomPoints(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

exports.actualizarPuntosJugadores = functions.pubsub
  .schedule("every 1 minutes") 
  .onRun(async (context) => {
    console.log("Inicio de actualización de puntos de jugadores");

    try {
      const jugadoresSnap = await db.ref("jugadores").once("value");
      const jugadores = jugadoresSnap.val();

      if (!jugadores) {
        console.log("No hay jugadores para actualizar");
        return null;
      }

      const nuevasPuntuaciones = {};
      for (const jugadorId in jugadores) {
        nuevasPuntuaciones[jugadorId] = getRandomPoints(-5, 20);
      }

      const updateJugadoresGlobal = {};
      for (const jugadorId in nuevasPuntuaciones) {
        updateJugadoresGlobal[`jugadores/${jugadorId}/puntos`] = nuevasPuntuaciones[jugadorId];
      }
      await db.ref().update(updateJugadoresGlobal);
      console.log("Actualizados puntos en jugadores globales");

      const usuariosSnap = await db.ref("usuarios").once("value");
      const usuarios = usuariosSnap.val();

      if (!usuarios) {
        console.log("No hay usuarios para actualizar");
        return null;
      }

      const updateUsuarios = {};
      const sumaPuntosPorUsuario = {};

      for (const uid in usuarios) {
        const equipoUsuarioSnap = await db.ref(`usuarios/${uid}/equipoUsuario`).once("value");
        const equipoUsuario = equipoUsuarioSnap.val();

        let sumaPuntos = 0;

        if (equipoUsuario) {
          for (const jugadorId in equipoUsuario) {
            if (nuevasPuntuaciones[jugadorId] !== undefined) {
              const puntosJugador = nuevasPuntuaciones[jugadorId];
              updateUsuarios[`usuarios/${uid}/equipoUsuario/${jugadorId}/puntos`] = puntosJugador;
              sumaPuntos += puntosJugador;
            }
          }
        }

        sumaPuntosPorUsuario[uid] = sumaPuntos;
      }

      await db.ref().update(updateUsuarios);
      console.log("Actualizados puntos en jugadores de equipoUsuario");

      const promesasUpdateTotal = Object.entries(sumaPuntosPorUsuario).map(async ([uid, sumaPuntos]) => {
        if (sumaPuntos === 0) return;
        const puntosUsuarioRef = db.ref(`usuarios/${uid}/puntos`);
        const snapshot = await puntosUsuarioRef.once("value");
        const puntosActuales = snapshot.val() || 0;
        const nuevosPuntos = puntosActuales + sumaPuntos;
        return puntosUsuarioRef.set(nuevosPuntos);
      });

      await Promise.all(promesasUpdateTotal);
      console.log("Actualizados puntos totales acumulados de usuarios");

      return null;
    } catch (error) {
      console.error("Error actualizando puntos:", error);
      return null;
    }
  });

