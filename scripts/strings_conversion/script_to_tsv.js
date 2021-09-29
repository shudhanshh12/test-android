var fs  = require("fs");

const output = [];

function main() {
  const english = fs.readFileSync('input/values/strings.xml').toString().split('\n');
  const hindi = fs.readFileSync('input/values-hi/strings.xml').toString().split('\n');
  const hinglish = fs.readFileSync('input/values-afh/strings.xml').toString().split('\n');
  const malayalam = fs.readFileSync('input/values-ml/strings.xml').toString().split('\n');
  const panjabi = fs.readFileSync('input/values-pa/strings.xml').toString().split('\n');

  english.forEach(function (line, index) {
    if (line.includes('string') && !line.includes('translatable="false"')) {
      const id = line.split('"')[1];
      const value1 = line.split('>')[1].replace('</string', '').replace(/\n$/, '');

      const value = line.substring(
        line.indexOf(">") + 1, 
        line.lastIndexOf("</string>")
      );

      console.log(`1. ${value1} \t ${value}`)

      addToArray('en', id, value);
    }
  });
  
  hindi.forEach(function (line, index) {
    if (line.includes('string') && !line.includes('translatable="false"')) {
      const id = line.split('"')[1];
      const value = line.substring(
        line.indexOf(">") + 1, 
        line.lastIndexOf("</string>")
      );
      addToArray('hi', id, value);
    }
  });

  hinglish.forEach(function (line, index) {
    if (line.includes('string') && !line.includes('translatable="false"')) {
      const id = line.split('"')[1];
      const value = line.substring(
        line.indexOf(">") + 1, 
        line.lastIndexOf("</string>")
      );
      addToArray('afh', id, value);
    }
  });

  panjabi.forEach(function (line, index) {
    if (line.includes('string') && !line.includes('translatable="false"')) {
      const id = line.split('"')[1];
      const value = line.substring(
        line.indexOf(">") + 1, 
        line.lastIndexOf("</string>")
      );
      addToArray('pa', id, value);
    }
  });

  malayalam.forEach(function (line, index) {
    if (line.includes('string') && !line.includes('translatable="false"')) {
      const id = line.split('"')[1];
      const value = line.substring(
        line.indexOf(">") + 1, 
        line.lastIndexOf("</string>")
      );
      addToArray('ml', id, value);
    }
  });

  if (fs.existsSync('./output.tsv')) {
    fs.unlinkSync('./output.tsv');
  }

  output.forEach(function (item) {
    fs.appendFileSync('./output.tsv', `${item.id}\t${item.en}\t${item.hi}\t${item.afh}\t${item.pa}\t${item.ml}\n`);
  });
}

function addToArray(lang, id, value) {
  let isContain = false;

  output.forEach(function (item, index) {
    if (item.id == id) {
      isContain = true;
      switch(lang) {
        case 'en':
            output[index].en = value
            break;
        case 'hi':
            output[index].hi = value
            break;
        case 'afh':
            output[index].afh = value
            break;
        case 'pa':
            output[index].pa = value
            break;
        case 'ml':
            output[index].ml = value
            break;
      }
    }
  });

  if (!isContain) {
    switch(lang) {
      case 'en':
          output.push({ en: value, id: id });
          break;
      case 'hi':
          output.push({ hi: value, id: id });
          break;
      case 'afh':
          output.push({ afh: value, id: id });
          break;
      case 'pa':
          output.push({ pa: value, id: id });
          break;
      case 'ml':
          output.push({ ml: value, id: id });
          break;
    }
  }
}

main();