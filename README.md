# [PhotoRoulette](https://filetransfer.io/data-package/446DrFqo#link)

## Télécharger le jeu

Vous pouvez télécharger l'apk du jeu [ici](https://filetransfer.io/data-package/446DrFqo#link). Attention le lien expire le 28/06/23. Il suffit ensuite d'installer l'application sur votre appareil Android.

## Présentation du jeu

PhotoRoulette est un jeu multijoueur sur smartphone inspiré d'un jeu du même nom connu pour avoir soulevé une polémique sur la vie privée en 2019. Le but du jeu est de deviner à qui appartient la photo choisie aléatoirement parmi les joueurs.

Pour résoudre le problème de vie privée, nous avons ajouté une fonctionnalité pour choisir manuellement la photo envoyée aux autres joueurs.

![logo](Sans titre-1.png)

## Structure du projet

Le projet est réalisé en Java sous [Android Studio](https://developer.android.com/studio).

La logique de jeu est gérée par le client et des appels à la database permettent de coordonner les différents clients.

La database utilisée est [Firebase](https://firebase.google.com/). Il s'agit d'une base de donnée sous forme de clé-valeur. Nous avons choisi cette database car elle est facilement implémentable sur un projet Android Studio et car elle possède une fonctionnalité appelée Firebase Storage. Il s'agit d'un cloud de stockage, élément nécessaire pour gérer les images envoyées par les joueurs et qui doivent être téléchargées par les joueurs.

## Déroulé d'une partie

Lorsque l'application est lancée, le joueur à le choix entre créer un lobby ou en rejoindre. Lorsqu'il créer un lobby, il rentre son pseudo et est envoyé dans le lobby. Ce lobby possède un identificant (code unique à 4 chiffres, facilement augmentable), ce qui permet à d'autres joueurs de le rejoindre.

Les joueurs présents dans un lobby peuvent démarrer la partie. Dans la phase de jeu, tous les joueurs choisissent une image (ils peuvent choisir manuellement dans leur galerie ou laisser l'aléatoire faire les choses) puis l'envoie à la base de données (Firebase Storage).

Dans la phase de choix, tous les joueurs reçoivent une image aléatoire (ils peuvent récupérer leur propre image pour le moment) et doivent choisir parmi la liste des joueurs à qui l'image téléchargée appartient.

Finalement, une page de résultat indique si le joueur a eu bon ou tort. Tous les joueurs peuvent soit recommencer une partie (ils reviennent dans la page de lobby) ou quitter l'application (les lobbies vides et les images reliées à aucun lobbies sont supprimés de la base de données).

## Améliorations possibles

Ajouter un score des joueurs et classement par lobby.

Faire une version iOS.

Créer des bornes de jeu permettant de jouer tous ensemble sur un même écran.
