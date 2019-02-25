import math


def __split_package_name(package_name):

   result = []

   end = package_name.find('.')

   while end > 0:

      result.append(package_name[0:end])

      end = package_name.find('.', end + 1)

   result.append(package_name)

   return result

def __resolve_packages(line, keyword):

   start = line.find(keyword) + len(keyword)

   end = line.find(';', start)

   if start < end:

      package_name = line[start:end].strip()

      return __split_package_name(package_name)

   return []

def resolve_package_usage(record, keyword):
      
  if record is not None:
        
        lines = record.split('\n')

        for line in lines: 

          if line.startswith(keyword):
                
                packages = __resolve_packages(line, keyword)

                for each in packages:
                      
                      yield (each, 1)

def compare_by_value(kv1, kv2):

   _, value1 = kv1

   _, value2 = kv2

   return value1 < value2

def resolve_package_help_score(record, keyword):

   count = 0

   package_name = ''

   if record is not None:

     lines = record.split('\n')

     for each in lines:

       if each.startswith(keyword):

         package_name = each

       if each in ['FIXME', 'TODO']:

         count += 1

     packages = (__resolve_packages(package_name, keyword))

     for package in packages:

         yield (package, count)

def calculate_composite_score(popular, help):

    for element in popular:

      if help.get(element[0]):

         composite = math.log(help.get(element[0])) * math.log(element[1])

         if composite > 0:

           yield (element[0], composite)
